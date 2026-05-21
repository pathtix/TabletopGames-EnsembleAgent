package players.llm.phase;

import core.AbstractGameState;
import core.components.GridBoard;
import games.connect4.Connect4GameState;
import games.poker.PokerGameState;
import games.sushigo.SGGameState;

public class LLMPhaseDetector {
    public static boolean isLLMPhase(AbstractGameState gameState, LLMPhaseConfig config) {
        return switch (gameState.getGameType()) {
            case Connect4 -> {
                if (config.connect4LLMFillThreshold <= 0.0) yield false;

                Connect4GameState c4gs = (Connect4GameState) gameState;
                GridBoard grid = c4gs.getGridBoard();

                int total = grid.getWidth() * grid.getHeight();
                int filled = 0;
                for (int y = 0; y < grid.getHeight(); y++)
                    for (int x = 0; x < grid.getWidth(); x++)
                        if (!grid.getElement(x, y).getComponentName().equals("."))
                            filled++;

                // if total filled smaller than threshold returns true which calls LLM player otherwise MCTS player.
                yield ((double) filled / total < config.connect4LLMFillThreshold);
            }
            case SushiGo -> {
                if (!config.sushiGoLLMUntilFullRotation) yield false;
                SGGameState sgs = (SGGameState) gameState;
                yield sgs.getDeckRotations() < sgs.getNPlayers() - 1;
            }
            case Catan -> gameState.getGamePhase().equals(config.catanLLMPhase);
            case Poker -> {
                PokerGameState.PokerGamePhase phase = (PokerGameState.PokerGamePhase) gameState.getGamePhase();
                yield phase.ordinal() <= config.pokerLLMPhase.ordinal();
            }
            default -> false;
        };
    }
}
