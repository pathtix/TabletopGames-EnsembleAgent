package games.connect4;

import core.AbstractGameState;
import core.interfaces.IStateFeatureJSON;
import core.components.GridBoard;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.LinkedHashMap;

public class Connect4LLMFeatures implements IStateFeatureJSON{

    @Override
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        Connect4GameState c4gs = (Connect4GameState) gameState;
        GridBoard grid = c4gs.getGridBoard();
        int width = grid.getWidth();
        int height = grid.getHeight();

        LinkedHashMap<String, Object> board = new LinkedHashMap<>();
        board.put("columns", "0 1 2 3 4 5 6 7");

        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++)
                row.append(grid.getElement(x, y).getComponentName()).append(" ");
            board.put("row" + y, row.toString().trim());
        }

        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        json.put("currentPlayer", playerID);
        json.put("symbol", playerID == 0 ? "x" : "o");
        json.put("board", board);

        return JSONValue.toJSONString(json);
    }
}