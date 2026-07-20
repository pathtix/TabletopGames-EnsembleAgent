package players.llm.phase;
import games.catan.CatanGameState;
import games.poker.PokerGameState;
import games.connect4.Connect4FillPhase;
import org.json.simple.JSONObject;
import utilities.JSONUtils;
import java.util.Set;
import java.util.EnumSet;

import org.apache.hadoop.shaded.com.google.gson.JsonArray;
import org.apache.hadoop.shaded.com.google.gson.JsonObject;
import org.json.simple.JSONArray;

public class LLMPhaseConfig {
    public int matchedBudgetMs = 500;

    // switch to MCTS once this fraction of the board is filled
    // public double connect4LLMFillThreshold = 0.0;
    public Set<Connect4FillPhase> connect4LLMPhases = EnumSet.noneOf(Connect4FillPhase.class);

    public boolean sushiGoLLMUntilFullRotation = true;

    public Set<CatanGameState.CatanGamePhase> catanLLMPhases = EnumSet.of(CatanGameState.CatanGamePhase.Setup);
    public boolean catanLLMTrade = false; // should LLM respond to trade offers

    // use LLM in pokerLLMPhase phases use MCTS in the phases that is not in pokerLLMPhase set
    public Set<PokerGameState.PokerGamePhase> pokerLLMPhases = EnumSet.of(PokerGameState.PokerGamePhase.Preflop);

    public LLMPhaseConfig() {}

    public LLMPhaseConfig(JSONObject json) {
        if (json.containsKey("matchedBudgetMs"))
            matchedBudgetMs = ((Long) json.get("matchedBudgetMs")).intValue();

        if (json.containsKey("connect4LLMPhases")) {
            JSONObject phasesJson = (JSONObject) json.get("connect4LLMPhases");
            connect4LLMPhases = EnumSet.noneOf(Connect4FillPhase.class);
            for (Object o : (JSONArray) phasesJson.get("value"))
                connect4LLMPhases.add(Connect4FillPhase.valueOf((String) o));
        }

        if (json.containsKey("sushiGoLLMUntilFullRotation"))
            sushiGoLLMUntilFullRotation = (boolean) json.get("sushiGoLLMUntilFullRotation");

        if (json.containsKey("catanLLMPhases")) {
            JSONObject phasesJson = (JSONObject) json.get("catanLLMPhases");
            catanLLMPhases = EnumSet.noneOf(CatanGameState.CatanGamePhase.class);
            for (Object o : (JSONArray) phasesJson.get("value"))
                catanLLMPhases.add(CatanGameState.CatanGamePhase.valueOf((String) o));
        }

        if  (json.containsKey("catanLLMTrade"))
            catanLLMTrade = (boolean) json.get("catanLLMTrade");

        if (json.containsKey("pokerLLMPhases")){
            JSONObject phasesJson = (JSONObject) json.get("pokerLLMPhases");
            pokerLLMPhases = EnumSet.noneOf(PokerGameState.PokerGamePhase.class);
            for (Object o : (JSONArray) phasesJson.get("value"))
                   pokerLLMPhases.add(PokerGameState.PokerGamePhase.valueOf((String) o));
        }

    }
}
