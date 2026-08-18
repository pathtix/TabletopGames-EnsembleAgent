package players.llm.phase;

import core.AbstractGameState;
import core.components.Counter;
import core.components.GridBoard;
import games.catan.CatanGameState;
import games.catan.CatanParameters;
import games.connect4.Connect4GameState;
import games.connect4.Connect4FillPhase;
import games.pandemic.PandemicConstants;
import games.pandemic.PandemicGame;
import games.pandemic.PandemicGameState;
import games.poker.PokerGameState;
import games.sushigo.SGGameState;
import utilities.Hash;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
                CatanParameters cap = (CatanParameters) cgs.getGameParameters();
                int pbep = gameState.getCurrentPlayer();

                if (config.catanLLMTradeResponse && cgs.getTradeOffer() != null) yield true; // llm if pbep has any trade offer

                if (config.catanLLMTradeInitiation &&
                        cgs.getTradeOffer() == null &&
                        gameState.getGamePhase() == core.CoreConstants.DefaultGamePhase.Main &&
                        cgs.nTradesThisTurn < cap.max_trade_actions_allowed &&
                        isTradingWorth(cgs, pbep, config))
                    yield true;

                yield config.catanLLMPhases.contains(gameState.getGamePhase());
            }
            case Poker -> config.pokerLLMPhases.contains(gameState.getGamePhase());
            case Pandemic -> {
                if (config.pandemicCuredThreshold <= 0) yield false;
                PandemicGameState pgs = (PandemicGameState) gameState;
                int cured = 0;
                for (String color : PandemicConstants.colors) {
                    if (((Counter) pgs.getComponent(Hash.GetInstance().hash("Disease " + color))).getValue() >= 1)
                        cured++;
                }
                yield cured < config.pandemicCuredThreshold;
            }
            default -> false;
        };
    }

    private static boolean isTradingWorth(CatanGameState cgs, int player, LLMPhaseConfig config) {
        Map<CatanParameters.Resource, Counter> resources = cgs.getPlayerResources(player);

        int max = 0;
        int min = Integer.MAX_VALUE;
        for (Map.Entry<CatanParameters.Resource, Counter> entry : resources.entrySet()) {
            if (entry.getKey() == CatanParameters.Resource.WILD) continue;

            int n = entry.getValue().getValue();
            if (n > max) max = n;
            if (n < min) min = n;
        }

        return max >= config.catanSurplusThreshold && min == 0;
    }
}
