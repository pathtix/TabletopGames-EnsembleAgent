package players.llm;
import llm.LLMAccess;
import players.PlayerParameters;

public class LLMActionParams extends PlayerParameters {
//    These can be edited from the .json file for the llm action player?
//    private final LLMAccess.LLM_MODEL modelType;
//    private final LLMAccess.LLM_SIZE modelSize;
//    private final String logFileName;
//    private final int maxStateChars;
    public LLMActionParams() {
        // TODO
    }

    @Override
    public void _reset() {
        // TODO
    }

    @Override
    protected LLMActionParams _copy() {
        return new LLMActionParams();
    }

}
