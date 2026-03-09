package players.llm;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import llm.LLMAccess;
import players.PlayerParameters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMActionPlayer extends AbstractPlayer {
    private final LLMAccess.LLM_MODEL modelType;
    private final LLMAccess.LLM_SIZE modelSize;
    private final String logFileName;
    private final int maxStateChars;

    private transient LLMAccess llmAccess;

    // adding these model based parameters to tunable parameter might help
    public LLMActionPlayer() {
//      this(new PlayerParameters(), LLMAccess.LLM_MODEL.GEMINI, LLMAccess.LLM_SIZE.SMALL, null, 3000);
        this(new PlayerParameters(), LLMAccess.LLM_MODEL.GEMINI, LLMAccess.LLM_SIZE.LARGE, null, 3000);
    }

    public LLMActionPlayer(LLMAccess.LLM_MODEL modelType, LLMAccess.LLM_SIZE modelSize) {
        this(new PlayerParameters(), modelType, modelSize, null, 3000);
    }

    public LLMActionPlayer(PlayerParameters parameters,
                           LLMAccess.LLM_MODEL modelType,
                           LLMAccess.LLM_SIZE modelSize,
                           String logFileName,
                           int maxStateChars) {
        super(parameters, "LLMActionPlayer");
        this.modelType = modelType;
        this.modelSize = modelSize;
        this.logFileName = logFileName;
        this.maxStateChars = Math.max(200, maxStateChars);
        this.llmAccess = new LLMAccess(modelType, modelSize, logFileName);
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        Integer actionId = queryActionId(gameState, possibleActions);
        if (isValidActionId(actionId, possibleActions.size()))
            return possibleActions.get(actionId);

        // fallback to random?
        // a rerun can be also made ?
        
        // to verify the move made by the llm, we can ask the llm again with a prompt like:
        // game state + action id + possible actions = `this is the action you returned, do you confirm it that it will be a *good* move or do you want to change it?`
        return possibleActions.get(rnd.nextInt(possibleActions.size()));
    }

    private Integer queryActionId(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String prompt = buildPrompt(gameState, possibleActions);
        String response = getLLMAccess().getResponse(prompt, modelType, modelSize);
        return parseActionId(response);
    }

    private String buildPrompt(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        String stateText = compactState(gameState);
        String actionsText = "";
        String promptText = "";

        StringBuilder actionsBuilder = new StringBuilder();
        for (int i = 0; i < possibleActions.size(); i++) {
            if (i > 0) {
                actionsBuilder.append("\n");
            }

            actionsBuilder.append(i).append(" ").append(compactActionString(possibleActions.get(i), gameState));
        }
        actionsText = actionsBuilder.toString();

        // A game related rules can be passed in prompt? (2-3 bullet points of core game rules?)
        String gameName = gameState.getGameType().name();

        if (gameName.equals("DotsAndBoxes")) {
            promptText =
                    """
                    You are controlling a Dots and Boxes game playing agent.
                    You are Player %d. Choose the action that is best for Player %d.
                    Choose exactly one legal action from the numbered list.

                    Output format:
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
        else if (gameName.equals("Poker"))
        {
            promptText =
                    """
                    You are a Texas Hold'em poker agent.
                    You are Player %d. Maximise your long run chip count.
            
                    Output contract:
                    ACTION_ID: <int>
            
                    Rules:
                    - Return exactly one line with no other text, punctuation, or explanation.
                    - Use exactly the prefix ACTION_ID:
                    - The id must be one of the listed action ids.
            
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
                    """.formatted(gameState.getCurrentPlayer(), stateText, actionsText);
        }
        return promptText;
    }

    private String compactState(AbstractGameState gameState) {
        String gameName = gameState.getGameType().name();

        if (gameName.equals("DotsAndBoxes")) {
            return compactDotsAndBoxesState(gameState);
        }
        else if (gameName.equals("Poker")) {
            return compactPokerState(gameState);
        }

        return gameState.toString();
    }

    private String compactPokerState(AbstractGameState gameState) {
        games.poker.PokerGameState pokerGameState = (games.poker.PokerGameState) gameState;
        int curentPlayerId = pokerGameState.getCurrentPlayer();
        int playerCount = pokerGameState.getNPlayers();

        // hole cards of LLMAgent
        StringBuilder myHoleCardsBuilder = new StringBuilder();
        for (core.components.FrenchCard card : pokerGameState.getPlayerDecks().get(curentPlayerId).getComponents()) {
            if (!myHoleCardsBuilder.isEmpty()) myHoleCardsBuilder.append(", ");
            myHoleCardsBuilder.append(card.toString());
        }
        String myHoleCards = myHoleCardsBuilder.toString();

        String communityCards = "None";
        if (pokerGameState.getCommunityCards().getSize() != 0) {
            StringBuilder communityCardsBuilder = new StringBuilder();
            for (core.components.FrenchCard card : pokerGameState.getCommunityCards().getComponents()) {
                if (!communityCardsBuilder.isEmpty()) communityCardsBuilder.append(", ");
                communityCardsBuilder.append(card.toString());
            }
            communityCards = communityCardsBuilder.toString();
        }

        int pot = 0;
        for (games.poker.components.MoneyPot mp : pokerGameState.getMoneyPots()) {
            pot += mp.getValue();
        }

        StringBuilder players = new StringBuilder();
        for (int j = 0; j < playerCount; j++) {
            players.append(String.format("P%d: stack=%d bet=%d%s%s%s",
                    j,
                    pokerGameState.getPlayerMoney()[j].getValue(),
                    pokerGameState.getPlayerBet()[j].getValue(),
                    pokerGameState.getPlayerFold()[j]  ? " FOLDED"  : "",
                    pokerGameState.getPlayerAllIn()[j] ? " ALL-IN"  : "",
                    j == pokerGameState.getBigId()     ? " [BB]"    : (j == pokerGameState.getSmallId() ? " [SB]" : "")
            ));

            if (j < playerCount - 1)
                players.append(" | ");
        }

        return String.format(
                "Phase=%s | MyHand=[%s] | Community=[%s] | Pot=%d | %s",
                gameState.getGamePhase(),
                myHoleCards,
                communityCards,
                pot,
                players
        );
    }

    private String compactDotsAndBoxesState(AbstractGameState gameState) {
        StringBuilder scoresBuilder = new StringBuilder();
        String scores = "";

        for (int player = 0; player < gameState.getNPlayers(); player++) {
            if (player > 0) {
                scoresBuilder.append(", ");
            }
            scoresBuilder.append("P").append(player).append("=").append(gameState.getGameScore(player));
        }
        scores = scoresBuilder.toString();

        /*
        gameState.getGameType() = [32mDotsAndBoxes[0m { minPlayers = 2 maxPlayers = 6 categories = [Simple, Abstract, TerritoryBuilding] mechanics = [Enclosure][34m GS = true[0m[34m FM = true[0m[34m Params = true[0m[34m GUI = true[0m }
        gameState.getGameType().name() = "DotsAndBoxes"
        we dont need any information other than game name from the gametype (maybe mechanics?)
         */

        String stateText = String.format(
                "Game=%s, CurrentPlayer=%d, Round=%d, Turn=%d, Scores=[%s], Status=%s, Phase=%s, State=%s",
                gameState.getGameType().name(),
                gameState.getCurrentPlayer(),
                gameState.getRoundCounter(),
                gameState.getTurnCounter(),
                scores,
                gameState.getGameStatus(),
                gameState.getGamePhase(),
            gameState
        );
        String oneLine = stateText.replaceAll("\\s+", " ").trim();

        if (!(oneLine.length() <= maxStateChars))
            return oneLine.substring(0, maxStateChars) + "...";

        return oneLine;
    }

    private String compactActionString(AbstractAction action, AbstractGameState gameState) {
        String actionText = "";
        try {
            actionText = action.getString(gameState);
        } catch (Exception e) {
            actionText = action.toString();
        }

        String oneLine = actionText.replaceAll("\\s+", " ").trim();
        Matcher matcher = Pattern.compile("\\((\\d+),(\\d+)\\)\\s*->\\s*\\((\\d+),(\\d+)\\)").matcher(oneLine);
        if (!matcher.find()) {
            return oneLine;
        }

        int x1 = Integer.parseInt(matcher.group(1));
        int y1 = Integer.parseInt(matcher.group(2));
        int x2 = Integer.parseInt(matcher.group(3));
        int y2 = Integer.parseInt(matcher.group(4));

        if (y1 == y2) {
            int y = y1;
            int x = Math.min(x1, x2);
            return "H_" + y + "_" + x;
        }
        if (x1 == x2) {
            int y = Math.min(y1, y2);
            int x = x1;
            return "V_" + y + "_" + x;
        }

        return "E_" + x1 + "_" + y1 + x2 + y2;
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

    private LLMAccess getLLMAccess() {
        if (llmAccess == null) {
            llmAccess = new LLMAccess(modelType, modelSize, logFileName);
        }
        return llmAccess;
    }

    @Override
    public AbstractPlayer copy() {
        LLMActionPlayer retValue = new LLMActionPlayer(parameters, modelType, modelSize, logFileName, maxStateChars);
        retValue.decorators = decorators;
        retValue.setName(this.toString());
        return retValue;
    }
}