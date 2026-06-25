package players.llm;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.interfaces.IActionListBuilder;
import core.interfaces.IStateFeatureJSON;
import games.GameType;
import games.catan.CatanGameState;
import games.sushigo.SGGameState;
import llm.LLMAccess;
import llm.LLMAccessGoogleGenAI;
import java.util.List;
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

        if (getParameters().verbose)
            System.out.printf("[%s] Action ID : %d selected randomly since LLM returned non valid action",this, actionId);

        return possibleActions.get(rnd.nextInt(possibleActions.size()));
    }

    // create llmaccess eagerly to remove the overhead of first API call of each game
    @Override
    public void initializePlayer(AbstractGameState gameState) {
        if (getParameters().modelType.equals(LLMAccess.LLM_MODEL.GEMINI)) {
            getLLMAccessGenAI();
            return;
        }

        getLLMAccess();
    }

    private Integer queryActionId(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String prompt = buildPrompt(gameState, possibleActions);
        long start = System.currentTimeMillis();
        String response = getResponse(prompt);
        long elapsedMs = System.currentTimeMillis() - start;

        if (getParameters().verbose)
        {
            System.out.printf("[%s] API call took %d ms (model=%s size=%s)%n", this, elapsedMs, getParameters().modelType, getParameters().modelSize);
            logIfInvalidAction(response, possibleActions.size(), gameState.getGameType());
        }

        return parseActionId(response, possibleActions.size(), gameState.getGameType());
    }

    private String getResponse(String prompt) {
        if (getParameters().modelType.equals(LLMAccess.LLM_MODEL.GEMINI)) {
            return getLLMAccessGenAI().getResponse(prompt, LLMAccessGoogleGenAI.modelNameForSize(getParameters().modelSize));
        }
        return getLLMAccess().getResponse(prompt);
    }

    private String buildPrompt(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String stateText = compactState(gameState);
        String actionsText = buildActionText(possibleActions, gameState);
        GameType gameName = gameState.getGameType();

        return switch (gameName) {
            case Poker -> buildPromptPoker(gameState, stateText, actionsText);
            case Connect4 -> buildPromptConnect4(gameState, stateText, actionsText);
            case SushiGo -> buildPromptSushiGo(gameState, stateText, actionsText);
            case Catan -> buildPromptCatan(gameState, stateText, actionsText);
            default -> "Game is not supported!";
        };
    }

    private String buildPromptPoker(AbstractGameState gameState, String stateText, String actionsText) {
        int llmPlayer = gameState.getCurrentPlayer();

        return """
        You are a World Class Texas Hold'em poker agent.
        You are Player %d. Maximise your long run chip count.

        Hand rankings (best to worst): Royal Flush > Straight Flush > Four of a Kind > Full House > Flush > Straight > Three of a Kind > Two Pair > Pair > High Card

        Strategy: Assess your hand strength (strong / medium / weak / drawing) using your hole cards and the community cards. The state JSON includes pot, costToCall, and potOddsPct, call only when your hand strength justifies the pot odds.

        Action glossary:
        - Check  : stay in without betting (only when no bet faces you)
        - Call   : match the current bet
        - Bet N  : open betting at N chips
        - Raise xM : raise to M times the current bet
        - AllIn  : commit all remaining chips
        - Fold   : surrender your hand

        Game state:
        %s

        Legal actions (id  action):
        %s

        Think in exactly one sentence
        Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        """.formatted(llmPlayer, stateText, actionsText);
    }

    private String buildPromptConnect4(AbstractGameState gameState, String stateText, String actionsText) {
        return """
        You are a Connect4 agent. You are Player %d (%s).

        Rules:
        - The id must be one of the listed action ids.
        - Use exactly the prefix ACTION_ID:

        Strategy:
        - Goal: get 4 of your pieces in a row (horizontal, vertical, or diagonal).
        - Block opponent threats, a line of 3 piece of opponent's must be answered.

        Gravity: a piece in column C lands on the lowest empty row in that column.

        Think in 2-3 sentences max, then end with ACTION_ID on the last line.
        Do NOT redraw the board. Do NOT repeat the action list.

        Board (x=P0, o=P1, .=empty):
        %s

        Legal actions (id col):
        %s

        Think in exactly one sentence, Do NOT redraw the board.
        Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
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

        Strategy:
        - If you have already played Tempura or Sashimi, PRIORITIZE picking more of the same to complete the set.
        - Build toward high-value combos early, MCTS will close it out after rotation.

        Rules:
        - The id must be one of the listed action ids.
        - Use exactly the prefix ACTION_ID: on the final line.
        - Do not output anything else.

        Game state:
        %s

        Legal actions (id card):
        %s

        Think in exactly one sentence.
        Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        """.formatted(gameState.getCurrentPlayer(), handNumber, totalHands, totalHands - 1, stateText, actionsText);
    }

    private String buildPromptCatan(AbstractGameState gameState, String stateText, String actionsText) {
        if (gameState.getGamePhase().equals(CatanGameState.CatanGamePhase.Robber))
            return buildPromptCatanRobber(gameState, stateText, actionsText);

        return buildPromptCatanSetup(gameState, stateText, actionsText);
    }

    private String buildPromptCatanSetup(AbstractGameState gameState, String stateText, String actionsText) {
        int round = gameState.getRoundCounter() + 1;

        return """
        You are placing a settlement in Catan setup (round %d/2).
        An MCTS agent will play the full game after setup, your only job is to give it the strongest possible starting position.

        A settlement sits on a corner where up to 3 tiles meet. Each tile has its OWN dice number and produces its resource whenever that number is rolled.

        Two dice are rolled each turn, so middle numbers are far more likely than extreme ones: 6 and 8 come up most often, then 5 and 9, then 4 and 10, then 3 and 11, while 2 and 12 are the rarest. A tile numbered 6 or 8 therefore produces much more frequently than one numbered 2 or 12.

        Example: a settlement on a wood-5 tile, a wood-9 tile, and a brick-6 tile makes 1 wood on a roll of 5, 1 wood on a roll of 9, and 1 brick on a roll of 6.

        Strategy:
        A good starting spot is one that produces frequently AND gives you a useful spread (diversity) of resources, without leaning too hard on a single dice number.

        In round 2, also consider what your first settlement already covers (shown in the state).

        Gamestate:
        %s

        Legal placements (each lists the tiles it touches as resource number):
        %s

        Think in exactly one sentence, Do NOT redraw the board.
        Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        """.formatted(round, stateText, actionsText);

        // return """
        // You are placing a settlement in Catan setup (round %d/2).
        // An MCTS agent will play the full game after setup, your only job is to give it the strongest possible starting position.

        // A settlement touches up to 3 tiles. Each tile produces its resource every time its number is rolled.
        // Example : If a settlement touches to for example 2 wood tiles and 1 brick tile, if that number is rolled, player gets 2 woods and 1 brick.

        // Strategy (Placement goals (in priority order)):
        // 1. Maximise total pip count : higher pips = more frequent resource production
        // 2. Maximise resource diversity : 3 different resources is better than duplicates
        // 3. In Round 2 avoid numbers already covered by your first settlement

        // Gamestate:
        // %s

        // Legal placements (id -> tiles touched -> total pips):
        // %s

        // Think in exactly one sentence, Do NOT redraw the board.
        // Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        // """.formatted(round, stateText, actionsText);

        // return """
        // You are placing a settlement in Catan setup (round %d/2).
        // An MCTS agent will play the full game after setup, your only job is to give it the strongest possible starting position.

        // A settlement sits on a corner where up to 3 tiles meet. Each tile has its OWN dice number and produces its resource whenever that number is rolled.

        // Example: a settlement on a wood-5 tile, a wood-9 tile, and a brick-6 tile makes 1 wood on a roll of 5, 1 wood on a roll of 9, and 1 brick on a roll of 6. Numbers near 7 (6 and 8) come up far more often than numbers near 2 or 12 the "pip" value shown for each tile reflects this (more pips = pays out more often).

        // Strategy:
        // A good starting spot is one that produces frequently AND gives you a useful spread (diversity) of resources, without leaning too hard on a single dice number.

        // In round 2, also consider what your first settlement already covers (shown in the state).

        // Gamestate:
        // %s

        // Legal placements (each lists the tiles it touches as resource number(pip)):
        // %s

        // Think in exactly one sentence, Do NOT redraw the board.
        // Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        // """.formatted(round, stateText, actionsText);
    }

    private String buildPromptCatanRobber(AbstractGameState gameState, String stateText, String actionsText) {
        int round = gameState.getRoundCounter() + 1;
        return """
        You are playing the robber in Catan (round %d). You rolled 7 (or a knight played) and you must move the robber. You then steal one random resource card from a player settled on that tile.

        Placing the robber on a tile BLOCKS it: that tile produces nothing for anyone settled on it until the robber moves again.

        How often a tile pays out depends only on its number, not its resource. Two dice make 6 and 8 the most common, then 5 and 9, then 4 and 10, then 3 and 11, while 2 and 12 are the rarest. So a tile numbered 6 or 8 is a far stronger block than one numbered 2, 11 or 12, whatever resource it carries.

        Your goal is to damage your strongest opponent by placing the robber on one of its frequent tiles. Do not just match their main resource, blocking that resource on a rare number like 11 denies them almost nothing.

        Don't block a tile you are also settled on ("blocks you").

        When the block value is similar, steal from whoever holds the most cards.

        "steal: none" means no card is there to be taken. (player has no resource cards)

        Gamestate (opponents' settlements show what each opponent produces):
        %s

        Legal robber moves:
        %s

        Think in exactly one sentence, Do NOT redraw the board.
        Your response MUST end with exactly: ACTION_ID: <Legal Action ID>
        """.formatted(round, stateText, actionsText);
    }

    private String buildActionText(List<AbstractAction> possibleActions, AbstractGameState gameState) {
        String className = getParameters().actionListClass;
        if (className != null && !className.isEmpty()) {
            try {
                IActionListBuilder builder = (IActionListBuilder) Class.forName(className).getDeclaredConstructor().newInstance();
                return builder.buildActionsText(possibleActions, gameState);
            } catch (Exception e) {
                System.out.println("Could not instantiate action list class : " + className);
            }
        }
        return "Class couldn't be found.";
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
        return "Class couldn't be found.";
    }

    private Integer parseActionId(String response, int actionCount, GameType gameType) {
        if (response == null) return null;

        java.util.List<String> patterns = new java.util.ArrayList<>();
        patterns.add("(?i)ACTION[_\\s]*ID\\s*[:=]\\s*(\\d+)"); // this one is universal for all games to parse the action

        if (gameType == GameType.Catan) {
            patterns.add("(\\d+)\\s*\\[[^\\]]*\\(\\d+pip\\)"); // 29 [BRICK 5(4pip)...
            patterns.add("(?i)\\b(?:location|position|tile|placement)\\s+(\\d+)"); // position 43, position x
        }

        for (String pattern : patterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(response);
            if (matcher.find()) {
                int id =  Integer.parseInt(matcher.group(1));
                if (id >= 0 && id < actionCount) return id;
            }
        }
        return null;
    }

    private boolean isValidActionId(Integer actionId, int actionCount) {
        return actionId != null && actionId >= 0 && actionId < actionCount;
    }

    private void logIfInvalidAction(String response, int actionCount, GameType gameType) {
        if (!getParameters().verbose) return;
        if (parseActionId(response, actionCount, gameType) == null) System.out.printf("[%s] Invalid response: %s%n", this, response);
    }

    private LLMAccess getLLMAccess() {
        if (llmAccess == null) {
            LLMAccess.LLM_MODEL model = getParameters().modelType;
            LLMAccess.LLM_SIZE size = getParameters().modelSize;
            String logFile = getParameters().logFileName;

            if (this.getParameters().verbose)
                System.out.printf("[%s] Creating LLMAccess: model=%s size=%s%n",this, model, size);
            llmAccess = new LLMAccess(model, size, logFile);
        }

        return llmAccess;
    }

    private LLMAccessGoogleGenAI getLLMAccessGenAI() {
        if (llmAccessGenAI == null) {
            String project = System.getenv("GEMINI_PROJECT");
            String apiKey = System.getenv("GOOGLE_AI_STUDIO_KEY");
            String location = "europe-west2";
            String logFile = getParameters().logFileName;

            if (getParameters().verbose)
                System.out.printf("[%s] Creating LLMAccessGoogleGenAI: model=%s%n", this, LLMAccessGoogleGenAI.modelNameForSize(getParameters().modelSize));

//            llmAccessGenAI = new LLMAccessGoogleGenAI(project, location, logFile); // Vertex AI
            llmAccessGenAI = new LLMAccessGoogleGenAI(apiKey, logFile);
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
