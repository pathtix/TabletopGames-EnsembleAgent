package games.dotsboxes;

import core.AbstractGameState;
import core.interfaces.IStateFeatureJSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class DBLLMFeatures implements IStateFeatureJSON {

    public String getObservationJson(AbstractGameState gameState, int playerID) {
        DBGameState dbgs = (DBGameState) gameState;
        JSONObject json = new JSONObject();

        json.put("currentPlayer", playerID);
        json.put("turn", gameState.getTurnCounter());

        // scores
        JSONObject scores = new JSONObject();
        for (int i = 0; i < gameState.getNPlayers(); i++)
            scores.put("p" + i, (int) gameState.getGameScore(i));
        json.put("scores", scores);

        // placed edges in H/V notation to match action format
        StringBuilder placed = new StringBuilder();
        for (var entry : dbgs.edgeToOwnerMap.entrySet()) {
            DBEdge edge = entry.getKey();
            int owner = entry.getValue();
            String notation = toEdgeNotation(edge);
            if (!placed.isEmpty()) placed.append(", ");
            placed.append(notation).append("(P").append(owner).append(")");
        }
        json.put("placedEdges", placed.toString());

        // completed cells by owner
        JSONObject cells = new JSONObject();
        for (var entry : dbgs.cellToOwnerMap.entrySet())
            cells.put(entry.getKey().toString(), entry.getValue());
        json.put("completedCells", cells);
        return json.toJSONString();
    }

    private String toEdgeNotation(DBEdge edge) {
        int x1 = (int) edge.from.getX(), y1 = (int) edge.from.getY();
        int x2 = (int) edge.to.getX(), y2 = (int) edge.to.getY();

        if (y1 == y2)
            return "H_" + y1 + "_" + Math.min(x1, x2);

        if (x1 == x2)
            return "V_" + Math.min(y1, y2) + "_" + x1;

        return edge.toString();
    }
}
