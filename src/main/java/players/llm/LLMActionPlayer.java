package players.llm;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import core.interfaces.IActionListBuilder;
import core.interfaces.IStateFeatureJSON;
import games.GameType;
import games.catan.CatanGameState;
import games.sushigo.SGGameState;
import games.sushigo.SGParameters;
import games.sushigo.cards.SGCard;
import llm.LLMAccess;
import llm.LLMAccessGoogleGenAI;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;

public class LLMActionPlayer extends AbstractPlayer {
    private transient LLMAccess llmAccess;
    private transient LLMAccessGoogleGenAI llmAccessGenAI;

    private final Map<String,String> promptCache = new HashMap<>();

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

    private String loadPromptTemplate(String key) {
        String path = getParameters().promptFile.get(key);
        String template;

        if (path == null)
            throw new IllegalStateException("No promptFile fot this key " + key + ". Following is available ones: " + getParameters().promptFile);

        if (promptCache.containsKey(path))
            return promptCache.get(path);

        // if prompt not already cached read from the txt files
        try {
            template = Files.readString(Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException("Prompt file not found at: " + path, e);
        }

        promptCache.put(path, template);
        return template;
    }

    private String fillPlaceholders(String template, Map<String, String> vars) {
        String output = template;
        for (var e : vars.entrySet()) output = output.replace("{{" + e.getKey() + "}}", e.getValue());
        return output;
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

        return fillPlaceholders(loadPromptTemplate("default"), Map.of(
            "player", String.valueOf(llmPlayer),
            "state", stateText,
            "actions", actionsText));
    }

    private String buildPromptConnect4(AbstractGameState gameState, String stateText, String actionsText) {
        int llmPlayer = gameState.getCurrentPlayer();
        return fillPlaceholders(loadPromptTemplate("default"), Map.of(
            "player", String.valueOf(llmPlayer),
            "symbol", llmPlayer == 0 ? "x" : "o",
            "state", stateText,
            "actions", actionsText));
    }

    private String formatDeckCounts(EnumMap<SGCard.SGCardType,Integer> counts) {
        StringBuilder sb = new StringBuilder();

        for (var e : counts.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append(' ').append(e.getValue());
        }
        return sb.toString();
    }

    private String buildPromptSushiGo(AbstractGameState gameState, String stateText, String actionsText) {
        SGGameState sggs = (SGGameState) gameState;
        SGParameters sggp = (SGParameters) sggs.getGameParameters();

        int handNumber = sggs.getDeckRotations() + 1;
        int totalHands = sggs.getNPlayers();

        // card counts static -> shows whole deck all time
        EnumMap<SGCard.SGCardType, Integer> deck = new EnumMap<>(SGCard.SGCardType.class);
        for (var e : sggp.nCardsPerType.entrySet())
            deck.merge(e.getKey().a, e.getValue(), Integer::sum);

        // card counts dynamic -> shows reamaining cards (whole deck - (seen cards + player's cards))
        EnumMap<SGCard.SGCardType, Integer> remainingDeck = new EnumMap<>(deck);
        // Map<SGCard.SGCardType, Counter>[] playedAll = sggs.getPlayedCardTypesAllGame();

        // reducing all played cards
        for (Deck<SGCard> played : sggs.getPlayedCards())
            for (SGCard card : played)
                remainingDeck.merge(card.type, -1, Integer::sum);

        // reducing the all discarded cards (that in discard pile)
        for (SGCard card : sggs.getDiscardPile())
            remainingDeck.merge(card.type, -1, Integer::sum);

        // reducing own hand
        for (SGCard c : sggs.getPlayerHands().get(gameState.getCurrentPlayer()))
            remainingDeck.merge(c.type, -1, Integer::sum);

        remainingDeck.replaceAll((k, v) -> Math.max(0, v));

        return fillPlaceholders(loadPromptTemplate("default"), Map.of(
            "player", String.valueOf(gameState.getCurrentPlayer()),
            "hand", String.valueOf(handNumber),
            "totalHands", String.valueOf(totalHands),
            "lastLLMHand", String.valueOf(totalHands - 1),
            "fullDeckCounts", formatDeckCounts(deck),
            "remainingDeckCounts", formatDeckCounts(remainingDeck),
            "state", stateText,
            "actions", actionsText));
    }

    private String buildPromptCatan(AbstractGameState gameState, String stateText, String actionsText) {
        CatanGameState cgs = (CatanGameState) gameState;
        String phaseKey;

        if (cgs.getTradeOffer() != null) {
            String me = "p" + gameState.getCurrentPlayer();
            String offerText = cgs.getTradeOffer().getString(gameState).replace(me, me + " (you)");
            String actions = actionsText.replace(me, me + " (you)");
            return fillPlaceholders(loadPromptTemplate("Trade"), Map.of(
                    "player", String.valueOf(gameState.getCurrentPlayer()),
                    "round", String.valueOf(gameState.getRoundCounter() + 1),
                    "offer", offerText,
                    "state", stateText,
                    "actions", actions
            ));
        }

        switch (gameState.getGamePhase()) {
            case CatanGameState.CatanGamePhase.Robber -> phaseKey = "Robber";
            case CatanGameState.CatanGamePhase.Setup -> phaseKey = "Setup";
            default -> phaseKey = "TradeInit"; // In main phase -> can initiate trade
        }

        return fillPlaceholders(loadPromptTemplate(phaseKey), Map.of(
                "player", String.valueOf(gameState.getCurrentPlayer()),
                "round", String.valueOf(gameState.getRoundCounter() + 1),
                "state", stateText,
                "actions", actionsText
        ));
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

           llmAccessGenAI = new LLMAccessGoogleGenAI(project, location, logFile); // Vertex AI
            // llmAccessGenAI = new LLMAccessGoogleGenAI(apiKey, logFile);
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
