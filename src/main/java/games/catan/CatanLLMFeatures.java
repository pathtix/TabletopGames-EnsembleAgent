package games.catan;

import core.AbstractGameState;
import core.components.BoardNodeWithEdges;
import core.components.Counter;
import core.interfaces.IStateFeatureJSON;
import games.catan.components.CatanTile;
import org.json.simple.JSONObject;

import java.util.*;

public class CatanLLMFeatures implements IStateFeatureJSON {
    @Override
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        CatanGameState cgs = (CatanGameState) gameState;
        JSONObject json = new JSONObject();

        int round = cgs.getRoundCounter() + 1;
        json.put("round", round);

        JSONObject myResources = new JSONObject();
        for (Map.Entry<CatanParameters.Resource, Counter> e : cgs.getPlayerResources(playerID).entrySet()) {
            if (e.getKey() == CatanParameters.Resource.WILD) continue;
            int n = e.getValue().getValue();
            if (n > 0) myResources.put(e.getKey().toString(), n);
        }
        json.put("myResources", myResources);

        CatanTile[][] board = cgs.getBoard();
        CatanParameters catanParameters = (CatanParameters) gameState.getGameParameters();

        Map<Integer, Integer> vertexOwner = new HashMap<>();
        Map<Integer, Set<String>> playerCoverage = new LinkedHashMap<>();

        for (BoardNodeWithEdges settlement : cgs.getSettlements()) {
            if (settlement.getOwnerId() < 0) // -1 for unowned settlements
                continue;

            vertexOwner.put(settlement.getComponentID(), settlement.getOwnerId());
        }

        for (CatanTile[] row : board) {
            for (CatanTile tile : row) {
                if (tile.getTileType() == CatanTile.TileType.SEA || tile.getTileType() == CatanTile.TileType.DESERT || tile.getNumber() == 0)
                    continue;

                CatanParameters.Resource resource = catanParameters.productMapping.get(tile.getTileType());
                if (resource == null) continue;
                for (int vid : tile.getVerticesBoardNodeIDs()) {
                    if (vertexOwner.containsKey(vid)) {
                        int owner = vertexOwner.get(vid);
                        Set<String> playerTiles = playerCoverage.get(owner);

                        if (playerTiles == null) {
                            playerTiles = new LinkedHashSet<>();
                            playerCoverage.put(owner, playerTiles);
                        }
                        playerTiles.add(resource + " " + tile.getNumber());
                        break;
                    }
                }
            }
        }

        if (round == 2) {
            Set<String> mine = playerCoverage.get(playerID);
            if (mine != null && !mine.isEmpty())
                json.put("myFirstSettlement", String.join(", ", mine));
        }

        boolean hasOpponentsBuild = playerCoverage.keySet().stream().anyMatch(k -> k != playerID);

        if (!hasOpponentsBuild) {
            json.put("opponentSettlements", "none placed yet");
            return json.toJSONString();
        }

        JSONObject opponentSettlements = new JSONObject();
        for (Map.Entry<Integer, Set<String>> e : playerCoverage.entrySet()) {
            if (e.getKey() == playerID) continue;
            opponentSettlements.put("p" + e.getKey(), String.join(", ", e.getValue()));
        }
        json.put("opponentSettlements", opponentSettlements);

        return json.toJSONString();
    }
}
