package players.llm.phase;

import core.AbstractGameState;
import core.components.GridBoard;
import games.catan.CatanGameState;
import games.connect4.Connect4GameState;
import games.connect4.Connect4FillPhase;
import games.poker.PokerGameState;
import games.sushigo.SGGameState;

public class LLMPhaseDetector {
    public static boolean isLLMPhase(AbstractGameState gameState, LLMPhaseConfig config) {
        return switch (gameState.getGameType()) {
            case Connect4 -> {
                if (config.connect4LLMPhases.isEmpty()) yield false;

                Connect4GameState c4gs = (Connect4GameState) gameState;
                GridBoard grid = c4gs.getGridBoard();

                int total = grid.getWidth() * grid.getHeight();
                int filled = 0;
                for (int y = 0; y < grid.getHeight(); y++)
                    for (int x = 0; x < grid.getWidth(); x++)
                        if (!grid.getElement(x, y).getComponentName().equals("."))
                            filled++;

                yield config.connect4LLMPhases.contains(Connect4FillPhase.of((double) filled / total));
            }
            case SushiGo -> {
                if (!config.sushiGoLLMUntilFullRotation) yield false;
                SGGameState sgs = (SGGameState) gameState;
                yield sgs.getDeckRotations() < sgs.getNPlayers() - 1;
            }
            case Catan -> {
                CatanGameState cgs = (CatanGameState) gameState;
                if (config.catanLLMTrade && cgs.getTradeOffer() != null) yield true; // llm if pbep has any trade offer
                yield config.catanLLMPhases.contains(gameState.getGamePhase());
            }
            case Poker -> config.pokerLLMPhases.contains(gameState.getGamePhase());
            default -> false;
        };
    }
}
