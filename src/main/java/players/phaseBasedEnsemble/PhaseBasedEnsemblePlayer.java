package players.phaseBasedEnsemble;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.components.GridBoard;
import games.catan.CatanGameState;
import games.connect4.Connect4GameState;
import games.poker.PokerGameState;
import games.sushigo.SGGameState;
import players.llm.LLMActionPlayer;
import players.mcts.MCTSPlayer;

import java.lang.reflect.Field;
import java.util.List;

public class PhaseBasedEnsemblePlayer extends AbstractPlayer {

    private transient LLMActionPlayer llmPlayer;
    private transient MCTSPlayer mctsPlayer;

    public PhaseBasedEnsemblePlayer() {
        this(new PhaseBasedEnsembleParams());
    }

    public PhaseBasedEnsemblePlayer(PhaseBasedEnsembleParams params) {
        super(params, "PhaseBasedEnsemblePlayer");
    }

    public PhaseBasedEnsembleParams getParameters() {
        return (PhaseBasedEnsembleParams) parameters;
    }

    @Override
    public void setForwardModel(AbstractForwardModel model) {
        super.setForwardModel(model);
        if (mctsPlayer != null) mctsPlayer.setForwardModel(model);
    }

    @Override
    public void initializePlayer(AbstractGameState gameState) {
        propagatePlayerID(getLLMPlayer());
        propagatePlayerID(getMCTSPlayer());
        getLLMPlayer().initializePlayer(gameState);
        getMCTSPlayer().initializePlayer(gameState);
    }

    private void propagatePlayerID(AbstractPlayer inner) {
        inner.setPlayerID(this.getPlayerID());
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        return selectPlayer(gameState)._getAction(gameState, possibleActions);
    }

    private AbstractPlayer selectPlayer(AbstractGameState gameState) {
        if (!useLLM(gameState)) return getMCTSPlayer();
        return getLLMPlayer();
    }

    private boolean useLLM(AbstractGameState gameState) {
        PhaseBasedEnsembleParams pbep = getParameters();
        return switch (gameState.getGameType().name()) {
            case "Connect4" -> {
                if (pbep.connect4LLMFillThreshold <= 0.0) yield false;

                Connect4GameState Connect4GameState = (Connect4GameState) gameState;
                GridBoard grid = Connect4GameState.getGridBoard();
                int total = grid.getWidth() * grid.getHeight();
                int filled = 0;
                for (int y = 0; y < grid.getHeight(); y++)
                    for (int x = 0; x < grid.getWidth(); x++)
                        if (!grid.getElement(x, y).getComponentName().equals(".")) filled++;

                // if total filled smaller than threshold returns true which calls LLM player otherwise MCTS player.
                yield ((double) filled / total < pbep.connect4LLMFillThreshold);
            }

            case "SushiGo" -> {
                if (!pbep.sushiGoLLMUntilFullRotation) yield false;
                SGGameState SushiGoGameState = (SGGameState) gameState;

                // before nPlayers - 1 rotations game is still hidden information so return LLM
                yield (SushiGoGameState.getDeckRotations() < SushiGoGameState.getNPlayers() - 1);
            }

            case "Catan" -> {
                if (!pbep.catanLLMDuringSetup) yield false;

                // return LLM player if the game phase of Catan is still in setup
                yield gameState.getGamePhase().equals(CatanGameState.CatanGamePhase.Setup);
            }

            case "Poker" -> {
                PokerGameState poker = (PokerGameState) gameState;
                int communityCards = poker.getCommunityCards().getSize();
                yield switch (pbep.pokerLLMPhase) {
                    case "pre-flop" -> communityCards == 0;
                    case "flop"     -> communityCards <= 3;
                    case "turn"     -> communityCards <= 4;
                    case "river"    -> communityCards <= 5;
                    default         -> false;
                };
            }
            default -> false;
        };
    }

    private LLMActionPlayer getLLMPlayer() {
        if (llmPlayer == null) llmPlayer = new LLMActionPlayer(getParameters().llmParams);
        return llmPlayer;
    }

    private MCTSPlayer getMCTSPlayer() {
        if (mctsPlayer == null) {
            mctsPlayer = new MCTSPlayer(getParameters().mctsParams);
            if (getForwardModel() != null) mctsPlayer.setForwardModel(getForwardModel());
        }
        return mctsPlayer;
    }

    @Override
    public AbstractPlayer copy() {
        PhaseBasedEnsemblePlayer copy = new PhaseBasedEnsemblePlayer((PhaseBasedEnsembleParams) parameters.copy());
        copy.setName(this.toString());
        return copy;
    }
}