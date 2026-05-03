package players.phaseBasedEnsemble;

import games.catan.CatanGameState;
import games.poker.PokerGameState;
import players.PlayerParameters;
import players.llm.LLMActionParams;
import players.mcts.MCTSParams;
import utilities.JSONUtils;

import java.util.Arrays;

public class PhaseBasedEnsembleParams extends PlayerParameters {
    public LLMActionParams llmParams  = new LLMActionParams();
    public MCTSParams mctsParams = new MCTSParams();


    public double connect4LLMFillThreshold = 0.0; // switch to MCTS once this fraction of the board is filled
    public boolean sushiGoLLMUntilFullRotation = true; // use LLM while cards haven't done a full rotation, switch to MCTS after

    // TODO: Robber and Trade phases can also be implemented
    public CatanGameState.CatanGamePhase catanLLMPhase = CatanGameState.CatanGamePhase.Setup; // use LLM only during setup phase later use MCTS
    public PokerGameState.PokerGamePhase pokerLLMPhase = PokerGameState.PokerGamePhase.Preflop; // use LLM till pokerLLMPhase use MCTS after

    public PhaseBasedEnsembleParams() {
        addTunableParameter("llmParamsFile", "");
        addTunableParameter("mctsParamsFile", "");
        addTunableParameter("connect4LLMFillThreshold", 0.0, Arrays.asList(0.0, 0.1, 0.25, 0.5));
        addTunableParameter("sushiGoLLMUntilFullRotation", true);
        addTunableParameter("catanLLMPhase", CatanGameState.CatanGamePhase.Setup, Arrays.asList(CatanGameState.CatanGamePhase.values()));
        addTunableParameter("pokerLLMPhase", PokerGameState.PokerGamePhase.Preflop, Arrays.asList(PokerGameState.PokerGamePhase.values()));
    }

    @Override
    public void _reset() {
        super._reset();
        String llmFile  = (String) getParameterValue("llmParamsFile");
        String mctsFile = (String) getParameterValue("mctsParamsFile");
        if (llmFile  != null && !llmFile.isEmpty())  llmParams  = JSONUtils.loadClassFromFile(llmFile);
        if (mctsFile != null && !mctsFile.isEmpty()) mctsParams = JSONUtils.loadClassFromFile(mctsFile);

        connect4LLMFillThreshold = (double)  getParameterValue("connect4LLMFillThreshold");
        sushiGoLLMUntilFullRotation = (boolean) getParameterValue("sushiGoLLMUntilFullRotation");
        catanLLMPhase = (CatanGameState.CatanGamePhase) getParameterValue("catanLLMPhase");
        pokerLLMPhase = (PokerGameState.PokerGamePhase) getParameterValue("pokerLLMPhase");
    }

    @Override
    protected PhaseBasedEnsembleParams _copy() {
        PhaseBasedEnsembleParams copy = new PhaseBasedEnsembleParams();
        copy.llmParams  = (LLMActionParams) llmParams.copy();
        copy.mctsParams = (MCTSParams) mctsParams.copy();
        return copy;
    }

    @Override
    public PhaseBasedEnsemblePlayer instantiate() {
        return new PhaseBasedEnsemblePlayer((PhaseBasedEnsembleParams) this.copy());
    }
}