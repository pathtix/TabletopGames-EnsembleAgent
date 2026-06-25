package llm;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class LLMAccessGoogleGenAI {

    private final Client geminiClient;
    private final String project = System.getenv("GEMINI_PROJECT");
    private final String location = "europe-west2";

    public static final String[] GEMINI_MODEL_NAMES = {
        "gemini-2.5-flash-lite",
        "gemini-2.5-flash",
        "gemini-3.1-flash-lite",
        "gemini-3-flash",
        "gemini-3.5-flash",
    };


    public static String modelNameForSize(LLMAccess.LLM_SIZE size) {
        return switch (size) {
            case SMALL -> GEMINI_MODEL_NAMES[0]; // gemini-2.5-flash-lite
            case LARGE -> GEMINI_MODEL_NAMES[1]; // gemini-2.5-flash
            default -> GEMINI_MODEL_NAMES[0]; // just get SMALL one
        };
    }

    // chat memory for genai package
    private final List<Content> chatHistory = new ArrayList<>();
    private int maxHistoryMessages = 20;

    private Content systemInstruction = null;

    // thinking budget would set to 0 for closing thinking in thinking models
    private int thinkingBudget = 1024;
    private boolean includeThinking = false;

    static OpenAiTokenCountEstimator tokenizer = new OpenAiTokenCountEstimator(
        "o200k_base"
    );
    long inputTokens = 0;
    long outputTokens = 0;

    private File logFile;
    private FileWriter logWriter;

    // vertex ai backend init
    public LLMAccessGoogleGenAI(
        String project,
        String location,
        String logFileName
    ) {
        String finalProject = (project != null && !project.isEmpty()) ? project : this.project;
        String finalLocation = (location != null && !location.isEmpty()) ? location : this.location;

        this.geminiClient = Client.builder()
            .vertexAI(true)
            .project(finalProject)
            .location(finalLocation)
            .build();

        initLog(logFileName);
    }

    // developer api backend init
    public LLMAccessGoogleGenAI(String apiKey, String logFileName) {
        this.geminiClient = Client.builder().apiKey(apiKey).build();
        initLog(logFileName);
    }

    private void initLog(String logFileName) {
        if (logFileName != null && !logFileName.isEmpty()) {
            logFile = new File(logFileName);
            try {
                logWriter = new FileWriter(logFile);
            } catch (Exception e) {
                System.out.println(
                    "Error creating log file: " + e.getMessage()
                );
            }
        }
    }

    public String getResponse(
        String query,
        String modelName,
        boolean useThinking,
        boolean useChatHistory
    ) {
        inputTokens += tokenizer.estimateTokenCountInText(query);

        Content userContent = Content.builder()
            .role("user")
            .parts(List.of(Part.builder().text(query).build()))
            .build();

        // TODO : I want to test the chat histroy before adding it into each call so for now it will be bound to boolean valude
        if (useChatHistory) {
            chatHistory.add(userContent);

            while (chatHistory.size() > maxHistoryMessages) {
                chatHistory.removeFirst();
            }
        }

        try {
            GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder();
            configBuilder.temperature(0.1f);

            // TODO: These are selected for now, after switching to Gemini 2.5 Flash these started to occur more
            configBuilder.maxOutputTokens(256);
//            configBuilder.frequencyPenalty(0.3f);

            if (useThinking) {
                configBuilder.thinkingConfig(
                    ThinkingConfig.builder()
                        .thinkingBudget(thinkingBudget)
                        .build()
                );
            } else {
                configBuilder.thinkingConfig(
                    ThinkingConfig.builder().thinkingBudget(0).build()
                );
            }

            if (systemInstruction != null) {
                configBuilder.systemInstruction(systemInstruction);
            }

            GenerateContentConfig generateContentConfig = configBuilder.build();

            List<Content> contentsToSend = useChatHistory
                ? chatHistory
                : List.of(userContent);
            GenerateContentResponse response =
                geminiClient.models.generateContent(
                    modelName,
                    contentsToSend,
                    generateContentConfig
                );
            String responseString = response.text();

            // add models answer to chat history as well, if chat history is gettin used
            if (useChatHistory) {
                Content assistantContent = Content.builder()
                    .role("model")
                    .parts(List.of(Part.builder().text(responseString).build()))
                    .build();
                chatHistory.add(assistantContent);
            }

            outputTokens += tokenizer.estimateTokenCountInText(responseString);

            if (logWriter != null) {
                String output = String.format("%nModel: %s%nQuery: %s%nResponse: %s%n", modelName, query, responseString);
                try {
                    logWriter.write(output);
                    logWriter.flush();
                }catch (Exception e) {
                    System.out.println("Error writing to log file: " + e.getMessage());
                }
            }

            return responseString;
        } catch (Exception e) {
            System.out.println("[GenAI] Error (" + e.getClass().getName() + "): " + e.getMessage());
            e.printStackTrace();

            if (useChatHistory) {
                if (!chatHistory.isEmpty()) {
                    chatHistory.removeLast();
                }
            }

            return "";
        }
    }

    // get model response without thikning and history
    public String getResponse(String query, String modelName) {
        return getResponse(query, modelName, false, false);
    }

    // TODO : maybe clean history between games
    public void clearHistory() {
        chatHistory.clear();
    }

    // method for adding system instrucitons (game details etc.) as a fake query/response
    public void addSystemContext(String prompt) {
        Content userContent = Content.builder()
            .role("user")
            .parts(List.of(Part.builder().text(prompt).build()))
            .build();
        chatHistory.add(userContent);

        Content ack = Content.builder()
            .role("model")
            .parts(List.of(Part.builder().text("Understood.").build()))
            .build();
        chatHistory.add(ack);
    }

    public void setSystemInstruction(String instruction) {
        this.systemInstruction = Content.builder()
            .role("user")
            .parts(List.of(Part.builder().text(instruction).build()))
            .build();
    }

    public void clearSystemInstruction() {
        this.systemInstruction = null;
    }

    public void setThinkingBudget(int budget) {
        this.thinkingBudget = budget;
    }

    public void setIncludeThinking(boolean include) {
        this.includeThinking = include;
    }

    public void setMaxHistoryMessages(int max) {
        this.maxHistoryMessages = max;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public void resetTokenCounters() {
        inputTokens = 0;
        outputTokens = 0;
    }
}
