package players.llm;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.components.BoardNodeWithEdges;
import core.components.Deck;
import core.components.FrenchCard;
import core.components.GridBoard;
import core.interfaces.IStateFeatureJSON;
import games.GameType;
import games.catan.CatanGameState;
import games.catan.CatanParameters;
import games.catan.actions.setup.PlaceSettlementWithRoad;
import games.catan.components.CatanTile;
import games.connect4.Connect4GameState;
import games.poker.PokerGameState;
import games.poker.components.MoneyPot;
import games.sushigo.SGGameState;
import games.sushigo.SGLLMFeatures;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import llm.LLMAccess;
import llm.LLMAccessGoogleGenAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMActionPlayer extends AbstractPlayer {
    private transient LLMAccess llmAccess;
    private transient LLMAccessGoogleGenAI llmAccessGenAI;

    public LLMActionPlayer() {
        this(new LLMActionParams());
    }

    public LLMActionPlayer(LLMActionParams params) {
        super(params, "LLMActionPlayer");
    }

    public LLMActionPlayer(LLMAccess.LLM_MODEL modelType, LLMAccess.LLM_SIZE llmSize) {
        this(new LLMActionParams());
        getParameters().setParameterValue("modelType", modelType);
        getParameters().setParameterValue("modelSize", llmSize);
    }

    public LLMActionParams getParameters() {
        return (LLMActionParams) parameters;
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        Integer actionId = queryActionId(gameState, possibleActions);
        if (isValidActionId(actionId, possibleActions.size()))
            return possibleActions.get(actionId);

        if (this.getParameters().verbose)
            System.out.printf("[%s] Action ID : %d selected randomly since LLM choose non valid action",this, actionId);

        return possibleActions.get(rnd.nextInt(possibleActions.size()));
    }

    // create llmaccess eagerly to remove the overhead of first API call of each game
    @Override
    public void initializePlayer(AbstractGameState gameState) {
        // TODO : Add a check if the model that stated in the parameters is a Gemini model get new LLMAccess otherwise create the old one
        // getLLMAccess();
        getLLMAccessGenAI();
    }

    private Integer queryActionId(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String prompt = buildPrompt(gameState, possibleActions);
        if (!this.getParameters().verbose) {
            // String response = getLLMAccess().getResponse(prompt);

            // response with no thinking, no history
            String response = getLLMAccessGenAI().getResponse(prompt, LLMAccessGoogleGenAI.modelNameForSize(getParameters().modelSize));
            return parseActionId(response);
        }

        long start = System.currentTimeMillis();
        String response = getLLMAccessGenAI().getResponse(prompt, LLMAccessGoogleGenAI.modelNameForSize(getParameters().modelSize));
        long elapsedMs = System.currentTimeMillis() - start;

        System.out.printf("[%s] API call took %d ms (model=%s size=%s)%n", this, elapsedMs, getParameters().modelType, getParameters().modelSize);
        logIfInvalidAction(response);
        return parseActionId(response);
    }

    private String buildPrompt(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String stateText = compactState(gameState);
        String actionsText = buildActionText(possibleActions, gameState);
        GameType gameName = gameState.getGameType();

        return switch (gameName) {
            case DotsAndBoxes -> buildPromptDotsAndBoxes(gameState, stateText, actionsText);
            case Poker -> buildPromptPoker(gameState, stateText, actionsText);
            case Connect4 -> buildPromptConnect4(gameState, stateText, actionsText);
            case SushiGo -> buildPromptSushiGo(gameState, stateText, actionsText);
            case Catan -> buildPromptCatan(gameState, stateText, actionsText);
            default -> "";
        };
    }

    private String buildActionText(List<AbstractAction> possibleActions, AbstractGameState gameState) {
        if (gameState.getGameType() == GameType.Catan)
            return buildCatanActionText(possibleActions, gameState);

        StringBuilder actionsBuilder = new StringBuilder();
        for (int i = 0; i < possibleActions.size(); i++) {
            if (i > 0) actionsBuilder.append("\n");
            actionsBuilder.append(i).append(" ").append(compactActionString(possibleActions.get(i), gameState));
        }
        return actionsBuilder.toString();
    }

    private String buildCatanActionText(List<AbstractAction> possibleActions, AbstractGameState gameState) {
        Map<String, Integer> seen = new LinkedHashMap<>();
        for (int i = 0; i < possibleActions.size(); i++) {
            String actionStr = compactActionCatan(possibleActions.get(i), gameState);
            // catanTileSignature for reducing duplicated tile data into one (with different edge, they create same but different in order)
            String tileSigniture = catanTileSignature(actionStr);
            if (!seen.containsKey(tileSigniture)) {
                seen.put(tileSigniture, i);
            }
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> e : seen.entrySet()) {
            if (!first) sb.append("\n");
            int id = e.getValue();
            sb.append(id).append(" ").append(compactActionCatan(possibleActions.get(id), gameState));
            first = false;
        }
        return sb.toString();
    }

    private String catanTileSignature(String actionStr) {
        int arrow = actionStr.indexOf("->");
        String tilesPart = (arrow > 0 ? actionStr.substring(0, arrow) : actionStr).replaceAll("[\\[\\]]", "").trim();

        List<String> tiles = new ArrayList<>();
        for (String t : tilesPart.split(",")) tiles.add(t.trim());

        Collections.sort(tiles);
        return String.join(",", tiles);
    }

    private String buildPromptDotsAndBoxes(AbstractGameState gameState, String stateText, String actionsText) {
        return """
        You are controlling a Dots and Boxes game playing agent.
        You are Player %d. Choose the action that is best for Player %d.
        Choose exactly one legal action from the numbered list.

        OUTPUT the final answer as following:
        ACTION_ID: <int>

        Rules:
        - Return exactly one line, do not include any other text, punctuation, or explanation.
        - Use exactly the prefix ACTION_ID:
        - The id must be one of the listed action ids.
        - Each action corresponds to drawing a line between two dots.
        - If you complete a box by drawing a line, you get an extra turn.

        State summary:
        %s

        Action legend:
        H_y_x means horizontal edge (x,y)->(x+1,y)
        V_y_x means vertical edge (x,y)->(x,y+1)

        Legal actions:
        %s
        """.formatted(gameState.getCurrentPlayer(), gameState.getCurrentPlayer(), stateText, actionsText);
    }

    private String buildPromptPoker(AbstractGameState gameState, String stateText, String actionsText) {
        int llmPlayer = gameState.getCurrentPlayer();

        return """
        You are a World Class Texas Hold'em poker agent.
        You are Player %d. Maximise your long run chip count.

        OUTPUT the final answer as following:
        ACTION_ID: <int>

        Rules:
        - Return exactly one line with no other text, punctuation, or explanation.
        - Use exactly the prefix ACTION_ID:
        - The id must be one of the listed action ids.

        Hand rankings (best to worst): Royal Flush > Straight Flush > Four of a Kind > Full House > Flush > Straight > Three of a Kind > Two Pair > Pair > High Card

        Game state:
        %s

        Action glossary:
        - Check  : stay in without betting (only when no bet faces you)
        - Call   : match the current bet
        - Bet N  : open betting at N chips
        - Raise xM : raise to M times the current bet
        - AllIn  : commit all remaining chips
        - Fold   : surrender your hand

        Legal actions (id  action):
        %s
        """.formatted(llmPlayer, stateText, actionsText);
    }

    private String buildPromptConnect4(AbstractGameState gameState, String stateText, String actionsText) {
        return """
        You are a Connect4 agent. You are Player %d (%s).
        Goal: get 4 of your pieces in a row (horizontal, vertical, or diagonal). Block opponent threats.

        Gravity: a piece in column C lands on the lowest empty row in that column.

        Think in 2-3 sentences max, then end with ACTION_ID on the last line.
        Do NOT redraw the board. Do NOT repeat the action list.

        OUTPUT the final answer as following:
        ACTION_ID: <int>

        The id must be one of the listed action ids.

        Board (x=P0, o=P1, .=empty):
        %s

        Legal actions (id col):
        %s
        """.formatted(gameState.getCurrentPlayer(), gameState.getCurrentPlayer() == 0 ? "x" : "o", stateText, actionsText);
    }

    private String buildPromptSushiGo(AbstractGameState gameState, String stateText, String actionsText) {
        SGGameState sg = (SGGameState) gameState;
        int handNumber = sg.getDeckRotations() + 1;
        int totalHands = sg.getNPlayers();

        return """
        You are the LLM component of a hybrid Sushi Go agent (Player %d).
        You handle the early hands of each round (%d/%d this round) while card information is still hidden.
        After hand %d, an MCTS play will take over with full card knowledge.
        Build a strong scoring position in these early picks so MCTS can close it out.

        Pick exactly 1 card from your hand, the rest passes to player on your left.
        Goal: maximise your score over 3 rounds.

        Scoring reference:
        - Maki (1/2/3 icons): end of round — most icons=6pts, second most=3pts (split ties)
        - Tempura: pair = 5pts, single=0
        - Sashimi: set of 3 = 10pts, fewer=0
        - Dumpling: 1 = 1, 2 = 3, 3 = 6, 4 = 10, 5+ = 15pts
        - Squid Nigiri: 3pts (9 on Wasabi) | Salmon Nigiri: 2pts (6) | Egg Nigiri: 1pt (3)
        - Wasabi: triples the NEXT nigiri played on it; worth 0 alone
        - Chopsticks: on a future turn, pick 2 cards instead of 1
        - Pudding: end of GAME - most=+6pts, fewest=-6pts (no penalty in 2-player)

        Set building rules:
        - If you have already played Tempura or Sashimi, PRIORITIZE picking more of the same to complete the set.

        Rules:
        - The id must be one of the listed action ids.
        - Use exactly the prefix ACTION_ID: on the final line.
        - Do not output anything else.

        Think in exactly one sentence, OUTPUT the final answer as following ACTION_ID: <int>

        Game state:
        %s

        Legal actions (id card):
        %s
        """.formatted(gameState.getCurrentPlayer(), handNumber, totalHands, totalHands - 1, stateText, actionsText);
    }

    private String buildPromptCatan(AbstractGameState gameState, String stateText, String actionsText) {
        int round = gameState.getRoundCounter() + 1;
        return """
        You are placing a settlement in Catan setup (round %d/2).
        An MCTS agent will play the full game after setup, your only job is to give it the strongest possible starting position.

        A settlement touches up to 3 tiles. Each tile produces its resource every time its number is rolled.
        Example : If a settlement touches to for example 2 wood tiles and 1 brick tile, if that number is rolled, player gets 2 woods and 1 brick.

        Placement goals (in priority order):
        1. Maximise total pip count : higher pips = more frequent resource production
        2. Maximise resource diversity : 3 different resources is better than duplicates
        3. In Round 2 avoid numbers already covered by your first settlement

        Gamestate:
        %s

        Legal placements (id -> tiles touched -> total pips):
        %s

        Think in exactly one sentence, Do NOT redraw the board.
        Your response MUST end with exactly: ACTION_ID: <int>.
        ACTION_ID: <int>
        """.formatted(round, stateText, actionsText);
    }

    private String compactState(AbstractGameState gameState) {
        String className = getParameters().stateFeatureClass;
        if (className != null && !className.isEmpty()) {
            try {
                IStateFeatureJSON extractor = (IStateFeatureJSON) Class.forName(className).getDeclaredConstructor().newInstance();
                return extractor.getObservationJson(gameState, gameState.getCurrentPlayer());
            } catch (Exception e) {
                System.out.println("Could not instantiate state feature class : " + className);
            }
        }
        return null;
    }

    private String compactActionString(AbstractAction action, AbstractGameState gameState) {
        return switch (gameState.getGameType()) {
            case DotsAndBoxes -> compactActionDotsAndBoxes(action, gameState);
            case Connect4 -> compactActionConnect4(action);
            case SushiGo -> compactActionSushiGo(action,  gameState);
            case Catan -> compactActionCatan(action, gameState);
            default -> defaultActionString(action, gameState);
        };
    }

    private String compactActionDotsAndBoxes(AbstractAction action, AbstractGameState gameState) {
        String oneLine = defaultActionString(action, gameState);
        Matcher matcher = Pattern.compile("\\((\\d+),(\\d+)\\)\\s*->\\s*\\((\\d+),(\\d+)\\)").matcher(oneLine);
        if (!matcher.find()) return oneLine;

        int x1 = Integer.parseInt(matcher.group(1));
        int y1 = Integer.parseInt(matcher.group(2));
        int x2 = Integer.parseInt(matcher.group(3));
        int y2 = Integer.parseInt(matcher.group(4));

        if (y1 == y2) return "H_" + y1 + "_" + Math.min(x1, x2);
        if (x1 == x2) return "V_" + Math.min(y1, y2) + "_" + x1;
        return "E_" + x1 + "_" + y1 + x2 + y2;
    }

    private String compactActionConnect4(AbstractAction action) {
        if (action instanceof core.actions.SetGridValueAction sgva)
            return "Col " + sgva.getX() + " (piece lands at row " + sgva.getY() + ")";
        return action.toString();
    }

    private String compactActionSushiGo(AbstractAction action, AbstractGameState gameState) {
        if (action instanceof ChooseCard cc) {
            String card = cc.getCard(gameState).toString();
            return cc.useChopsticks ? card + " (+chopsticks)" : card;
        }
        return defaultActionString(action, gameState);
    }

    private String compactActionCatan(AbstractAction action, AbstractGameState gameState) {
        if (!(action instanceof PlaceSettlementWithRoad pswr))
            return defaultActionString(action, gameState);

        CatanGameState catanGameState = (CatanGameState) gameState;
        CatanTile[][] board = catanGameState.getBoard();
        CatanParameters catanParameters = (CatanParameters) gameState.getGameParameters();

        // add all touching tiles into a list
        List<CatanTile> touching = new ArrayList<>();
        touching.add(board[pswr.x][pswr.y]);

        // get neigbour tiles that is arround to this tile
        for (int[] neighbor : board[pswr.x][pswr.y].getNeighboursOnVertex(pswr.vertex)) {
            if (neighbor[0] >= 0 &&
                neighbor[0] < board.length &&
                neighbor[1] >= 0 &&
                neighbor[1] < board[0].length
            )
                touching.add(board[neighbor[0]][neighbor[1]]);
        }

        int totalPips = 0;
        Set<String> resources = new LinkedHashSet<>();
        List<String> parts = new ArrayList<>();

        // calculate up to 3 tiles around a vertex, its resources and pips of each tile
        for (CatanTile tile : touching) {
            if (tile.getTileType() == CatanTile.TileType.SEA || tile.getTileType() == CatanTile.TileType.DESERT || tile.getNumber() == 0)
                continue;

            CatanParameters.Resource resource = catanParameters.productMapping.get(tile.getTileType());
            if (resource == null) continue;

            int pips = 6 - Math.abs(tile.getNumber() - 7);
            totalPips += pips;
            resources.add(resource.name());
            parts.add(resource + " " + tile.getNumber() + "(" + pips + "pip)");
        }

        if (parts.isEmpty()) return "SEA/DESERT only";
        return String.format("[%s] -> %d pips, %d resources", String.join(", ", parts), totalPips, resources.size());
    }

    private String defaultActionString(AbstractAction action, AbstractGameState gameState) {
        try {
            return action.getString(gameState).replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return action.toString();
        }
    }

    private Integer parseActionId(String response) {
        if (response == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("(?i)ACTION_ID\\s*:\\s*(-?\\d+)").matcher(response);
        if (!matcher.find()) {
            return null;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidActionId(Integer actionId, int actionCount) {
        return actionId != null && actionId >= 0 && actionId < actionCount;
    }

    private void logIfInvalidAction(String response) {
        if (!this.getParameters().verbose) return;
        if (parseActionId(response) == null) System.out.printf("[%s] Invalid response: %s%n", this, response);
    }

    private LLMAccess getLLMAccess() {
        if (llmAccess == null) {
            LLMAccess.LLM_MODEL model = (LLMAccess.LLM_MODEL) getParameters().getParameterValue("modelType");
            LLMAccess.LLM_SIZE  size = (LLMAccess.LLM_SIZE) getParameters().getParameterValue("modelSize");
            String logFile  = (String) getParameters().getParameterValue("logFileName");

            if (this.getParameters().verbose)
                System.out.printf("[%s] Creating LLMAccess: model=%s size=%s%n",this, model, size);
            llmAccess = new LLMAccess(model, size, logFile);
        }

        return llmAccess;
    }

    private LLMAccessGoogleGenAI getLLMAccessGenAI() {
        if (llmAccessGenAI == null) {
            String project = System.getenv("GEMINI_PROJECT");
            String location = "europe-west9";
            String logFile = (String) getParameters().getParameterValue("logFileName");

            if (this.getParameters().verbose)
                System.out.printf("[%s] Creating LLMAccessGoogleGenAI: model=%s%n", this, LLMAccessGoogleGenAI.modelNameForSize(getParameters().modelSize));
            llmAccessGenAI = new LLMAccessGoogleGenAI(project, location, logFile);
        }
        return llmAccessGenAI;
    }

    @Override
    public AbstractPlayer copy() {
        LLMActionPlayer retValue = new LLMActionPlayer((LLMActionParams) parameters.copy());
        retValue.decorators = decorators;
        retValue.setName(this.toString());
        retValue.llmAccess = this.llmAccess;
        retValue.llmAccessGenAI = this.llmAccessGenAI;
        return retValue;
    }
}
