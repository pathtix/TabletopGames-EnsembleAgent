package games.connect4;

import core.AbstractGameState;
import core.interfaces.IStateFeatureJSON;
import core.components.GridBoard;
import org.json.simple.JSONObject;

public class Connect4LLMFeatures implements IStateFeatureJSON{

    @Override
    public String getObservationJson(AbstractGameState gameState, int playerID) {
        Connect4GameState c4gs = (Connect4GameState) gameState;
        GridBoard grid = c4gs.getGridBoard();
        int width = grid.getWidth();
        int height = grid.getHeight();

        JSONObject json = new JSONObject();
        json.put("currentPlayer", playerID);
        json.put("symbol", playerID == 0 ? "x" : "o");

        StringBuilder board = new StringBuilder();
        board.append(" ");
        for (int x = 0; x < width; x++) board.append(x).append(" ");
        board.append("\n");
        for (int y = 0; y < height; y++) {
            board.append(y).append(" ");
            for (int x = 0; x < width; x++)
                board.append(grid.getElement(x, y).getComponentName()).append(" ");
            board.append("\n");
        }
        json.put("board", board.toString().trim());

        return json.toJSONString();
    }
}