package players.llm.phase;
import games.catan.CatanGameState;
import games.poker.PokerGameState;
import org.json.simple.JSONObject;
import utilities.JSONUtils;
import java.util.Set;
import java.util.EnumSet;

import org.apache.hadoop.shaded.com.google.gson.JsonArray;
import org.json.simple.JSONArray;

public class LLMPhaseConfig {
    public int matchedBudgetMs = 500;

    // switch to MCTS once this fraction of the board is filled
    public double connect4LLMFillThreshold = 0.0;
    public boolean sushiGoLLMUntilFullRotation = true;

    // TODO: Robber and Trade phases can also be implemented
    public Set<CatanGameState.CatanGamePhase> catanLLMPhases = EnumSet.of(CatanGameState.CatanGamePhase.Setup);

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

        if (json.containsKey("catanLLMPhases")) {
            JSONObject phasesJson = (JSONObject) json.get("catanLLMPhases");
            catanLLMPhases = EnumSet.noneOf(CatanGameState.CatanGamePhase.class);
            for (Object o : (JSONArray) phasesJson.get("value"))
                catanLLMPhases.add(CatanGameState.CatanGamePhase.valueOf((String) o));
        }

        if (json.containsKey("pokerLLMPhase"))
            pokerLLMPhase = JSONUtils.loadClassFromJSON((JSONObject) json.get("pokerLLMPhase"));
    }
}
