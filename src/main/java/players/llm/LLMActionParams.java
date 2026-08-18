package players.llm;
import llm.LLMAccess;
import players.PlayerParameters;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class LLMActionParams extends PlayerParameters {
    public LLMAccess.LLM_MODEL modelType = LLMAccess.LLM_MODEL.GEMINI;
    public LLMAccess.LLM_SIZE modelSize = LLMAccess.LLM_SIZE.LARGE;
    public String logFileName = "json/experiments/outputDir/LLMActionPlayerLog.txt";
    public String stateFeatureClass = "";
    public String actionListClass = "";
    public int maxStateChars = 3000;
    public boolean verbose = false;

    public Map<String, String> promptFile = new HashMap<>();

    public LLMActionParams() {
        addTunableParameter("modelType", LLMAccess.LLM_MODEL.GEMINI, Arrays.asList(LLMAccess.LLM_MODEL.values()));
        addTunableParameter("modelSize",  LLMAccess.LLM_SIZE.LARGE, Arrays.asList(LLMAccess.LLM_SIZE.values()));
        addTunableParameter("logFileName", "json/experiments/OutputDirectory/LLMActionPlayerLog.txt");
        addTunableParameter("maxStateChars",  3000, Arrays.asList(500, 1000, 2000, 3000, 5000));
        addStaticParameter("promptFile", new HashMap<String, String>());
        addTunableParameter("stateFeatureClass", "");
        addTunableParameter("actionListClass", "");
        addTunableParameter("verbose", false);

    }

    @Override
    public void _reset() {
        super._reset();
        modelType = (LLMAccess.LLM_MODEL) getParameterValue("modelType");
        modelSize = (LLMAccess.LLM_SIZE) getParameterValue("modelSize");
        promptFile = (Map<String, String>) getParameterValue("promptFile");
        logFileName = (String) getParameterValue("logFileName");
        maxStateChars = (int) getParameterValue("maxStateChars");
        stateFeatureClass = (String) getParameterValue("stateFeatureClass");
        actionListClass = (String) getParameterValue("actionListClass");
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
