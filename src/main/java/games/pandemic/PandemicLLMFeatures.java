package games.pandemic;

import core.AbstractGameState;
import core.components.*;
import core.interfaces.IStateFeatureJSON;
import core.properties.Property;
import core.properties.PropertyColor;
import core.properties.PropertyIntArray;
import core.properties.PropertyString;
import org.apache.logging.log4j.core.Core;
import org.json.simple.JSONObject;
import utilities.Hash;

import java.util.*;

import static games.pandemic.PandemicConstants.*;
import static core.CoreConstants.*;

public class PandemicLLMFeatures implements IStateFeatureJSON {
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        PandemicGameState pgs = (PandemicGameState) gameState;
        PandemicParameters pp = (PandemicParameters) gameState.getGameParameters();
        PandemicTurnOrder pto = (PandemicTurnOrder) pgs.getTurnOrder();
        JSONObject json = new JSONObject();
        GraphBoard world =  pgs.getWorld();
        int nPlayers = pgs.getNPlayers();

        int round = pgs.getRoundCounter() + 1;
        json.put("round", round);

        int actionsRemaining = pp.getnActionsPerTurn() - pto.getTurnStep();
        json.put("actionsRemainingThisTurn", actionsRemaining);

        // all player infos
        List<Map<String, Object>> playerInfo = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < nPlayers; i++) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("playerID", i);
            info.put("self (you)",  i == playerID);
            info.put("role", pgs.getPlayerRole(i));
            info.put("location", ((PropertyString) pgs.getComponent(playerCardHash, i).
                    getProperty(playerLocationHash)).value);

            // hand cards and color numbers
            Deck<Card> hand = (Deck<Card>) pgs.getComponent(playerHandHash, i);
            Map<String, Integer> colorCounts = new HashMap<>();
            for (String color: colors) colorCounts.put(color, 0);
            List<String> cards = new ArrayList<>();
            for (Card card : hand.getComponents()) {
                Property np = card.getProperty(nameHash);
                String cardName = (np != null) ? ((PropertyString) np).value : card.getComponentName(); // claude
                cards.add(cardName);

                Property color = card.getProperty(colorHash);
                if (color != null) colorCounts.merge(((PropertyColor) color).valueStr, 1 , Integer::sum);
            }
            info.put("handColorCounts", colorCounts);
            info.put("cards", cards);
            playerInfo.add(info);
        }

        json.put("playerInfo", playerInfo);

        // e.g {blue: uncured, yellow: cured, black: uncured, red: eradicated}
        Map<String, String> cureProgress =  new HashMap<String, String>();
        for (String color : colors) {
            int status = ((Counter) pgs.getComponent(Hash.GetInstance().hash("Disease " + color))).getValue();
            String statusLabel = (status == 2) ? "eradicated" : (status == 1) ? "cured" : "uncured";
            cureProgress.put(color, statusLabel);
        }

        json.put("cureProgress", cureProgress);

        // infected cities list
        List<Map<String, Object>> infectedCities = new ArrayList<>();

        for (BoardNode city : world.getBoardNodes()) {
            PropertyIntArray infection = (PropertyIntArray) city.getProperty(infectionHash);
            if (infection == null) continue;

            int[] cubes = infection.getValues();
            String cityName = ((PropertyString) city.getProperty(nameHash)).value;
            for (int colorID = 0; colorID < colors.length; colorID++) {
                if (cubes[colorID] > 0) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("city", cityName);
                    entry.put("color", colors[colorID]);
                    entry.put("cubes", cubes[colorID]);
                    entry.put("isInfectionAtRisk", cubes[colorID] >= pp.getMaxCubesPerCity());
                    infectedCities.add(entry);
                }
            }
        }

        json.put("infectedCities", infectedCities);

        // research cities list
        ArrayList<String> researchStations = pgs.researchStationLocations;

        json.put("researchStations", researchStations);
        // losing information (Outbreak counter, Cubes remaining per colour in supply (blue: 20/24), Player deck remaining, Infection rate (2 cards/turn, rising), Epidemics Seen)
        Map<String, Object> losingInfo = new LinkedHashMap<>();

        int outbreakCounter = ((Counter) pgs.getComponent(outbreaksHash)).getValue();
        int infectionRateStep =  ((Counter) pgs.getComponent(infectionRateHash)).getValue();
        int epidemicsSeen = infectionRateStep;

        Map<String, String> cubesRemaining = new LinkedHashMap<>();
        int cubeMax = pp.getnInitialDiseaseCubes();
        for (String color : colors) {
            int cubeCounter = ((Counter) pgs.getComponent(Hash.GetInstance().hash("Disease Cube " + color))).getValue();
            cubesRemaining.put(color, cubeCounter + "/" + cubeMax);
        }

        int playerDeckRemaining = ((Deck<Card>) pgs.getComponent(playerDeckHash)).getSize();
        int cardDrawPerTurn = pp.getInfectionRate()[infectionRateStep];

        losingInfo.put("outbreaks", outbreakCounter + "/" + pp.getLoseMaxOutbreak());
        losingInfo.put("cubesRemaining", cubesRemaining);
        losingInfo.put("playerDeckRemaining", playerDeckRemaining);
        losingInfo.put("infectionRateCardsPerTurn", cardDrawPerTurn);
        losingInfo.put("epidemicsSeen", epidemicsSeen + "/" + pp.getnEpidemicCards());

        json.put("losingInfo", losingInfo);
        return json.toJSONString().replace("\\/","/");
    }
}
