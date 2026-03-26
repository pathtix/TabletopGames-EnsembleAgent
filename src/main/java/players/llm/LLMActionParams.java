package players.llm;
import llm.LLMAccess;
import players.PlayerParameters;
import java.util.Arrays;

// TODO : Later, other params like Temperature, K etc. also can be added into tunable parameters to optimise further.

public class LLMActionParams extends PlayerParameters {
    public LLMAccess.LLM_MODEL modelType = LLMAccess.LLM_MODEL.GEMINI;
    public LLMAccess.LLM_SIZE modelSize = LLMAccess.LLM_SIZE.LARGE;
    public String logFileName = "json/experiments/outputDir/LLMActionPlayerLog.txt";
    public int maxStateChars = 3000;
    public boolean verbose = false;

    public LLMActionParams() {
        addTunableParameter("modelType", LLMAccess.LLM_MODEL.GEMINI, Arrays.asList(LLMAccess.LLM_MODEL.values()));
        addTunableParameter("modelSize",  LLMAccess.LLM_SIZE.LARGE, Arrays.asList(LLMAccess.LLM_SIZE.values()));
        addTunableParameter("logFileName", "json/experiments/outputDir/LLMActionPlayerLog.txt");
        addTunableParameter("maxStateChars",  3000, Arrays.asList(500, 1000, 2000, 3000, 5000));
        addTunableParameter("verbose", false);
    }

    @Override
    public void _reset() {
        super._reset();
        modelType = (LLMAccess.LLM_MODEL) getParameterValue("modelType");
        modelSize = (LLMAccess.LLM_SIZE) getParameterValue("modelSize");
        logFileName = (String) getParameterValue("logFileName");
        maxStateChars = (int) getParameterValue("maxStateChars");
        verbose = (boolean) getParameterValue("verbose");
    }

    @Override
    protected LLMActionParams _copy() {
        return new LLMActionParams();
    }

    @Override
    public LLMActionPlayer instantiate() {
        return new LLMActionPlayer((LLMActionParams) this.copy());
    }
}
