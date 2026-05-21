package players.llm.phase;
import games.catan.CatanGameState;
import games.poker.PokerGameState;
import org.json.simple.JSONObject;
import utilities.JSONUtils;

public class LLMPhaseConfig {
    public int matchedBudgetMs = 500;

    // switch to MCTS once this fraction of the board is filled
    public double connect4LLMFillThreshold = 0.0;
    public boolean sushiGoLLMUntilFullRotation = true;

    // TODO: Robber and Trade phases can also be implemented
    public CatanGameState.CatanGamePhase catanLLMPhase = CatanGameState.CatanGamePhase.Setup;

    // use LLM until pokerLLMPhase (pokerLLMPhase included) use MCTS after
    public PokerGameState.PokerGamePhase pokerLLMPhase = PokerGameState.PokerGamePhase.Preflop;

    public LLMPhaseConfig() {}

    public LLMPhaseConfig(JSONObject json) {
        if (json.containsKey("matchedBudgetMs"))
            matchedBudgetMs = ((Long) json.get("matchedBudgetMs")).intValue();

        if (json.containsKey("connect4LLMFillThreshold"))
            connect4LLMFillThreshold = ((Number) json.get("connect4LLMFillThreshold")).doubleValue();

        if (json.containsKey("sushiGoLLMUntilFullRotation"))
            sushiGoLLMUntilFullRotation = (boolean) json.get("sushiGoLLMUntilFullRotation");

        if (json.containsKey("catanLLMPhase"))
            catanLLMPhase = JSONUtils.loadClassFromJSON((JSONObject) json.get("catanLLMPhase"));

        if (json.containsKey("pokerLLMPhase"))
            pokerLLMPhase = JSONUtils.loadClassFromJSON((JSONObject) json.get("pokerLLMPhase"));
    }
}
