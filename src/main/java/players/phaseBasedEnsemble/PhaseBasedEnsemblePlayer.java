package players.phaseBasedEnsemble;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.components.GridBoard;
import games.connect4.Connect4GameState;
import games.poker.PokerGameState;
import games.sushigo.SGGameState;
import java.util.List;
import players.llm.LLMActionPlayer;
import players.llm.phase.LLMPhaseDetector;
import players.mcts.MCTSPlayer;

public class PhaseBasedEnsemblePlayer extends AbstractPlayer {

    private transient LLMActionPlayer llmPlayer;
    private transient MCTSPlayer mctsPlayer;

    public PhaseBasedEnsemblePlayer() {
        this(new PhaseBasedEnsembleParams());
    }

    public PhaseBasedEnsemblePlayer(PhaseBasedEnsembleParams params) {
        super(params, "PhaseBasedEnsemblePlayer");
        llmPlayer = new LLMActionPlayer(params.llmParams);
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
        PhaseBasedEnsembleParams params = getParameters();
        if (!useLLM(gameState)) {
            return getMCTSPlayer()._getAction(gameState, possibleActions);
        }

        if (params.useFairBudget) {
            long timeBudgetMs = params.phaseConfig.matchedBudgetMs;

            long start = System.currentTimeMillis();
            AbstractAction llmAction = getLLMPlayer()._getAction(gameState, possibleActions);
            long elapsed = System.currentTimeMillis() - start;

            if (elapsed > timeBudgetMs) {
                // to prevent overhead of another mcts action creation
                // return getMCTSPlayer()._getAction(gameState, possibleActions);
                return possibleActions.get(rnd.nextInt(possibleActions.size()));
            }

            return llmAction;
        }

        return getLLMPlayer()._getAction(gameState, possibleActions);
    }

    private boolean useLLM(AbstractGameState gameState) {
        return LLMPhaseDetector.isLLMPhase(gameState, getParameters().phaseConfig);
    }

    private LLMActionPlayer getLLMPlayer() {
        if (llmPlayer == null) llmPlayer = new LLMActionPlayer(
            getParameters().llmParams
        );
        return llmPlayer;
    }

    private MCTSPlayer getMCTSPlayer() {
        if (mctsPlayer == null) {
            mctsPlayer = new MCTSPlayer(getParameters().mctsParams);
            if (getForwardModel() != null) mctsPlayer.setForwardModel(
                getForwardModel()
            );
        }
        return mctsPlayer;
    }

    @Override
    public AbstractPlayer copy() {
        PhaseBasedEnsemblePlayer copy = new PhaseBasedEnsemblePlayer(
            (PhaseBasedEnsembleParams) parameters.copy()
        );
        copy.setName(this.toString());
        copy.llmPlayer = this.llmPlayer;
        return copy;
    }
}
