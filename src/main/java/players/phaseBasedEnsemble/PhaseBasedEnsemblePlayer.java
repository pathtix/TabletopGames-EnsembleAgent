package players.phaseBasedEnsemble;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;

import java.util.List;

public class PhaseBasedEnsemblePlayer extends AbstractPlayer {

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
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        AbstractPlayer activePlayer = selectPlayer(gameState);
        return activePlayer._getAction(gameState, possibleActions);
    }

    // TODO: implement game-specific phase detection and player selection.
    private AbstractPlayer selectPlayer(AbstractGameState gameState) {
        // TODO: detect game phase (e.g. CatanGamePhase.Setup, fill ratio for Connect4)
        // TODO: return LLM player or MCTS player accordingly
        throw new UnsupportedOperationException("selectPlayer not yet implemented");
    }

    // TODO: implement per-game phase detection logic.
    private int getGamePhase(AbstractGameState gameState) {
        // TODO: return 0 = opening/setup, 1 = midgame, 2 = endgame (game-specific thresholds)
        throw new UnsupportedOperationException("getGamePhase not yet implemented");
    }

    @Override
    public AbstractPlayer copy() {
        PhaseBasedEnsemblePlayer copy = new PhaseBasedEnsemblePlayer((PhaseBasedEnsembleParams) parameters.copy());
        copy.setName(this.toString());
        return copy;
    }
}