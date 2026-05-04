package games.sushigo;

import core.AbstractGameState;
import core.interfaces.IStateFeatureJSON;
import games.sushigo.cards.SGCard;
import org.json.simple.JSONObject;
import utilities.Pair;

import java.util.Arrays;
import java.util.List;


public class SGLLMFeatures implements IStateFeatureJSON {

    @Override
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        SGGameState sggs = (SGGameState) gameState;
        JSONObject json = new JSONObject();

        // general game information
        json.put("round", sggs.getRoundCounter() + 1);
        json.put("totalRounds", 3);
        json.put("handNumber", sggs.getDeckRotations() + 1);
        json.put("totalHands", sggs.getNPlayers());

        // llm player hand
        StringBuilder hand = new StringBuilder();
        sggs.getPlayerHands().get(playerID).getComponents()
            .forEach(c -> hand.append(c).append(" "));

        json.put("myHand", hand.toString().trim());

        // llm player's played cards
        StringBuilder played = new StringBuilder();
        sggs.getPlayedCards().get(playerID).getComponents()
            .forEach(c -> played.append(c).append(" "));

        json.put("myPlayedCards", played.toString().trim());

        // game information - set progres
        int tempura = sggs.getPlayedCardTypes(SGCard.SGCardType.Tempura, playerID).getValue();
        int sashimi = sggs.getPlayedCardTypes(SGCard.SGCardType.Sashimi,  playerID).getValue();
        json.put("tempuraPlayed", tempura);
        json.put("tempuraNeedForNextPair", (tempura % 2 == 0) ? 2 : 1);
        json.put("sashimiPlayed", sashimi);
        json.put("sashimiNeedForNextTriple", (sashimi % 3 == 0) ? 3 : (3 - sashimi % 3));

        // scores and pudding counts
        JSONObject scores = new JSONObject();
        JSONObject puddings = new JSONObject();
        for (int i = 0; i < sggs.getNPlayers(); i++) {
            scores.put("p" + i, (int) gameState.getGameScore(i));
            puddings.put("p" + i, sggs.getPlayedCardTypes(SGCard.SGCardType.Pudding, i).getValue());
        }
        json.put("scores", scores);
        json.put("puddings", puddings);

        // opponent's played cards
        for (int i = 0; i < sggs.getNPlayers(); i++) {
            if (i == playerID) continue;
            StringBuilder oppPlayed = new StringBuilder();
            sggs.getPlayedCards().get(i).getComponents()
                .forEach(c -> oppPlayed.append(c).append(" "));

                json.put("opp" + i + "PlayedCards", oppPlayed.toString().trim());
        }

        return json.toJSONString();

    }
}
