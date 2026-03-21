package players.phaseBasedEnsemble;

import players.PlayerParameters;

public class PhaseBasedEnsembleParams extends PlayerParameters {

    // TODO: add fields for inner player configs (e.g. LLM params, MCTS params)
    // TODO: add fields for game-specific stage thresholds

    public PhaseBasedEnsembleParams() {
        // TODO: register tunable parameters via addTunableParameter(...)
    }

    @Override
    public void _reset() {
        super._reset();
        // TODO: read back tunable parameters via getParameterValue(...)
    }

    @Override
    protected PhaseBasedEnsembleParams _copy() {
        return new PhaseBasedEnsembleParams();
    }

    @Override
    public PhaseBasedEnsemblePlayer instantiate() {
        return new PhaseBasedEnsemblePlayer((PhaseBasedEnsembleParams) this.copy());
    }
}