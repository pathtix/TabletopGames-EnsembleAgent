package games.poker;

import core.AbstractGameState;
import core.components.FrenchCard;
import core.interfaces.IStateFeatureJSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class PokerLLMFeatures implements IStateFeatureJSON {

    @Override
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        PokerGameState pgs = (PokerGameState) gameState;
        JSONObject json = new JSONObject();

        json.put("phase", gameState.getGamePhase().toString());

        // hole cards
        StringBuilder hole = new StringBuilder();
        for (FrenchCard card : pgs.getPlayerDecks().get(playerID).getComponents()) {
            if (!hole.isEmpty()) hole.append(", ");
            hole.append(card.toString());
        }

        json.put("myHoleCards", hole.toString());

        // community cards
        if (pgs.getCommunityCards().getSize() == 0) {
            json.put("communityCards", "None");
        } else {
            StringBuilder cc = new StringBuilder();
            for (FrenchCard card : pgs.getCommunityCards().getComponents()) {
                if (!cc.isEmpty()) cc.append(", ");
                cc.append(card.toString());
            }
            json.put("communityCards", cc.toString());
        }

        // players
        JSONArray players = new JSONArray();
        for (int i = 0; i < pgs.getNPlayers(); i++) {
            JSONObject p = new JSONObject();
            p.put("id", i);
            p.put("stack", pgs.getPlayerMoney()[i].getValue());
            p.put("bet", pgs.getPlayerBet()[i].getValue());
            p.put("folded", pgs.getPlayerFold()[i]);
            p.put("allIn", pgs.getPlayerAllIn()[i]);
            p.put("role", i == pgs.getBigId() ? "BB" : (i == pgs.getSmallId() ? "SB" : ""));
            players.add(p);
        }
        json.put("players", players);

        // pot odds
        int pot = 0;
        for (var mp : pgs.getMoneyPots()) pot += mp.getValue();
        int myBet = pgs.getPlayerBet()[playerID].getValue();
        int maxBet = 0;
        for (int j = 0; j < pgs.getNPlayers(); j++)
            maxBet = Math.max(maxBet, pgs.getPlayerBet()[j].getValue());
        int costToCall = maxBet - myBet;

        json.put("pot", pot);
        if (costToCall <= 0) {
            json.put("potOdds", "no bet to face, check is free");
        } else {
            int potOdds = (int) Math.round(100.0 * costToCall / (pot + costToCall));
            json.put("costToCall", costToCall);
            json.put("potOddsPct", potOdds);
        }

        return json.toJSONString();
    }
}
