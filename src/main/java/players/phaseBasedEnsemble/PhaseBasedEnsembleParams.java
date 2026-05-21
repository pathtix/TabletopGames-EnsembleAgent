package players.phaseBasedEnsemble;

import games.catan.CatanGameState;
import games.poker.PokerGameState;
import players.PlayerParameters;
import players.llm.LLMActionParams;
import players.llm.phase.LLMPhaseConfig;
import players.mcts.MCTSParams;
import utilities.JSONUtils;

import java.util.Arrays;

public class PhaseBasedEnsembleParams extends PlayerParameters {
    public LLMActionParams llmParams  = new LLMActionParams();
    public MCTSParams mctsParams = new MCTSParams();
    public LLMPhaseConfig phaseConfig = new LLMPhaseConfig();
    public boolean llmTimeoutFallback = true;

    public PhaseBasedEnsembleParams() {
        addTunableParameter("llmParamsFile", "");
        addTunableParameter("mctsParamsFile", "");
        addTunableParameter("phaseConfigFile", "");
        addTunableParameter("llmTimeoutFallback", true);
    }

    @Override
    public void _reset() {
        super._reset();
        String llmFile  = (String) getParameterValue("llmParamsFile");
        String mctsFile = (String) getParameterValue("mctsParamsFile");
        String phaseConfigFile = (String) getParameterValue("phaseConfigFile");
        if (llmFile  != null && !llmFile.isEmpty())  llmParams  = JSONUtils.loadClassFromFile(llmFile);
        if (mctsFile != null && !mctsFile.isEmpty()) mctsParams = JSONUtils.loadClassFromFile(mctsFile);
        if (phaseConfigFile != null && !phaseConfigFile.isEmpty()) phaseConfig = JSONUtils.loadClassFromFile(phaseConfigFile);
        llmTimeoutFallback = (boolean) getParameterValue("llmTimeoutFallback");
    }

    @Override
    protected PhaseBasedEnsembleParams _copy() {
        PhaseBasedEnsembleParams copy = new PhaseBasedEnsembleParams();
        copy.llmParams = (LLMActionParams) llmParams.copy();
        copy.mctsParams = (MCTSParams) mctsParams.copy();
        copy.llmTimeoutFallback = llmTimeoutFallback;
        copy.phaseConfig = phaseConfig;
        return copy;
    }

    @Override
    public PhaseBasedEnsemblePlayer instantiate() {
        return new PhaseBasedEnsemblePlayer((PhaseBasedEnsembleParams) this.copy());
    }
}