package players.phaseBasedEnsemble;

import players.PlayerParameters;
import players.llm.LLMActionParams;
import players.mcts.MCTSParams;
import utilities.JSONUtils;

import java.io.File;
import java.util.Arrays;

public class PhaseBasedEnsembleParams extends PlayerParameters {
    public LLMActionParams llmParams  = new LLMActionParams();
    public MCTSParams mctsParams = new MCTSParams();


    public double connect4LLMFillThreshold = 0.0; // switch to MCTS once this fraction of the board is filled
    public boolean sushiGoLLMUntilFullRotation = true; // use LLM while cards haven't done a full rotation, switch to MCTS after

    // TODO: More analysis needed maybe for trading llm can be used too
    public boolean catanLLMDuringSetup = true; // use LLM only during setup phase later use MCTS
    public String pokerLLMPhase = "pre-flop"; // use LLM till pokerLLMPhase use MCTS after

    public PhaseBasedEnsembleParams() {
        addTunableParameter("llmParamsFile",  "json/experiments/EnsemblePlayers/LLMActionPlayer.json");
        addTunableParameter("mctsParamsFile", getMCTSPlayerPath("json/experiments/EnsemblePlayers")); // game spesific MCTS player json
        addTunableParameter("connect4LLMFillThreshold", 0.0, Arrays.asList(0.0, 0.1, 0.25, 0.5));
        addTunableParameter("sushiGoLLMUntilFullRotation", true);
        addTunableParameter("catanLLMDuringSetup", true);
        addTunableParameter("pokerLLMPhase", "pre-flop", Arrays.asList("pre-flop", "flop", "turn", "river"));
    }

    // this is not the best way I think, but gets the non LLMActionPlayer.json in the EnsemblePlayer folder as MCTS agent
    private String getMCTSPlayerPath(String ensemblePlayerFolderPath) {
        File folder = new File(ensemblePlayerFolderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0) return null;
        for (File file : listOfFiles) {
            if (file.getName().endsWith(".json") && !file.getName().equals("LLMActionPlayer.json")) {
                return file.getPath();
            }
        }
        return null;
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
        catanLLMDuringSetup = (boolean) getParameterValue("catanLLMDuringSetup");
        pokerLLMPhase = (String) getParameterValue("pokerLLMPhase");
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