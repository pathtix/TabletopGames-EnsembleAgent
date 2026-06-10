package llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModelName;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.model.mistralai.MistralAiChatModel;

// import dev.langchain4j.memory.chat.MessageWindowChatMemory; is needed for memory based chat history
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.JSONUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class LLMAccess {
    static VertexAiGeminiChatModel[] geminiModel = new VertexAiGeminiChatModel[8];
    static MistralAiChatModel[] mistralModel = new MistralAiChatModel[3];
    static OpenAiChatModel[] openaiModel = new OpenAiChatModel[3];
    static AnthropicChatModel[] anthropicModel = new AnthropicChatModel[3];


    static OpenAiTokenCountEstimator tokenizer = new OpenAiTokenCountEstimator("o200k_base");

    String mistralToken = System.getenv("MISTRAL_TOKEN");
    String geminiProject = System.getenv("GEMINI_PROJECT");
    String openaiToken = System.getenv("OPENAI_TOKEN");
    String anthropicToken = System.getenv("ANTHROPIC_TOKEN");
    String openRouterToken = System.getenv("OPENROUTER_TOKEN");

    File logFile;
    FileWriter logWriter;

    String geminiLocation = "europe-west2";

    // String llamaLocationLarge = "us-east5";  // Required for Llama 4 Maverick
    String llamaLocationLarge = "us-central1";
    String llamaLocationSmall = "us-central1";

    // Local LLM Settings
    String localLLMBaseURL = "http://10.60.89.11";
    String localLLMBasePort = "1234";
    String[] localLLMModelNames = new String[2];
    boolean localLLMModelLoaded = false;

    LLM_MODEL modelType;
    LLM_SIZE modelSize;

    MessageWindowChatMemory chatMemory;

    long inputTokens = 0;
    long outputTokens = 0;

    public enum LLM_MODEL {
        GEMINI,
        MISTRAL,
        OPENAI,
        ANTHROPIC,
        LLAMA,
        LOCAL_LLM,
        OPENROUTER
    }

    public enum LLM_SIZE {
        SMALL,
        LARGE,
        REASONING
    }


    /**
     * Constructor for LLMAccess
     *
     * @param modelType   - the model to use as the default (this can be overridden in the getResponse method)
     * @param modelSize   - the size of the model to use (this can be overridden in the getResponse method)
     * @param logFileName - the name of the log file to write to. this will include all the messages sent to any LLM
     */
    public LLMAccess(LLM_MODEL modelType, LLM_SIZE modelSize, String logFileName) {
        if (logFileName != null && !logFileName.isEmpty()) {
            logFile = new File(logFileName);
            try {
                logWriter = new FileWriter(logFile);
            } catch (Exception e) {
                System.out.println("Error creating log file: " + e.getMessage());
            }
        }

        this.modelType = modelType;
        this.modelSize = modelSize;
        if (geminiProject != null && !geminiProject.isEmpty()) {
            try {
                geminiModel[0] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-2.0-flash-lite")
                        .build();
                geminiModel[1] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        //      .temperature(1.0f)  // between 0 and 2; default 1.0 for pro-1.5
                        //       .topK(40) // some models have a three-stage sampling process. topK; then topP; then temperature
                        //       .topP(0.94f)  // 1.5 default is 0.64; the is the sum of probability of tokens to sample from
                        //     .maxOutputTokens(1000)  // max replay size (max is 8192)
                        // .modelName("gemini-1.5-pro")   // $1.25 per million characters input, $0.3125 per million output
                        .modelName("gemini-2.0-flash") // $0.075 per million characters output, $0.01875 per million characters input
                        .build();
                geminiModel[2] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-2.5-flash-preview-05-20")
                        .build();
                geminiModel[3] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-2.5-flash-lite") // $0.10 per million characters input, $0.40 per million output
                        .build();
                geminiModel[4] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-2.5-flash") // $0.30 per million characters input, $1.25 per million output
                        .build();
                geminiModel[5] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-2.5-pro") // $2.50 per million characters input, $10.00 per million output
                        .build();
                geminiModel[6] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-3-flash-preview") // $0.50 per million input tokens, $3.00 per million output tokens
                        .build();
                geminiModel[7] = VertexAiGeminiChatModel.builder()
                        .project(geminiProject)
                        .location(geminiLocation)
                        .modelName("gemini-3.1-flash-lite-preview") // $0.25 per million input tokens, $1.50 per million output tokens
                        .build();
            } catch (Error e) {
                System.out.println("Error creating Gemini model: " + e.getMessage());
            }
        }

        // local models
        localLLMModelNames[0] = "qwen/qwen3.5-9b"; // -> thinking model, works on gguf
        localLLMModelNames[1] = "qwen/qwen3-4b-2507"; // -> non thinking model works on mlx as well

        if (mistralToken != null && !mistralToken.isEmpty()) {
            mistralModel[0] = MistralAiChatModel.builder()
                    .modelName(MistralAiChatModelName.MISTRAL_SMALL_LATEST)
                    .apiKey(mistralToken)
                    .build();
            mistralModel[1] = MistralAiChatModel.builder()
                    .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST)
                    .apiKey(mistralToken)
                    .build();
            // $2 per million input tokens, $6 per million output tokens
        }

        if (openaiToken != null && !openaiToken.isEmpty()) {
            openaiModel[0] = OpenAiChatModel.builder()
                    .modelName(OpenAiChatModelName.GPT_4_O_MINI) // $0.15 per million input tokens, $0.6 per million output tokens
                    .apiKey(openaiToken)
                    .build();
            openaiModel[1] = OpenAiChatModel.builder()
                    .modelName(OpenAiChatModelName.GPT_4_O) // $5 per million input tokens, $15 per million output tokens
                    .apiKey(openaiToken)
                    .build();
            openaiModel[2] = OpenAiChatModel.builder()
                    .modelName(OpenAiChatModelName.O1) // $6 per million input tokens, $18 per million output tokens
                    .apiKey(openaiToken)
                    .build();
        }

        // TODO : Fix this pricing lists for CLAUDE 3.5 etc. (they needed to change when VertexAI package updated)
        if (anthropicToken != null && !anthropicToken.isEmpty()) {
            anthropicModel[0] = AnthropicChatModel.builder()
                    .modelName(AnthropicChatModelName.CLAUDE_SONNET_4_20250514)
                    .apiKey(anthropicToken)                                     // But 3.5 is deprecated
                    .maxTokens(4096)
                    .build();
            anthropicModel[1] = AnthropicChatModel.builder()
                    .modelName(AnthropicChatModelName.CLAUDE_SONNET_4_5_20250929) // $3 per million input tokens, $15 per million output tokens
                    .apiKey(anthropicToken)
                    .maxTokens(8192)
                    .build();
        }
    }

    /**
     * Gets some text from the specified model
     *
     * @param query     the prompt sent to the model
     * @param modelType the LLM model to use
     * @return The full text returned by the model; or an empty string if no valid model
     */
    public String getResponse(String query, LLM_MODEL modelType, LLM_SIZE modelSize) {
        String response = "";
        inputTokens += tokenizer.estimateTokenCountInText(query);

        if (modelType == LLM_MODEL.LLAMA) {
            // do this the hardcore way
            response = getResponseWithLowLevelHttp(query, modelSize);
        }
        else if (modelType == LLM_MODEL.LOCAL_LLM) {
            response = getResponseWithLocalLLMEndpoints(query);
        }
        else if (modelType == LLM_MODEL.OPENROUTER) {
            response = getResponseWithOpenRouterHttp(query);
        }
        else {
            ChatModel modelToUse = switch (modelType) {
                case MISTRAL -> modelSize == LLM_SIZE.SMALL ? mistralModel[0] : mistralModel[1];
                case GEMINI -> modelSize == LLM_SIZE.SMALL ? geminiModel[3] : geminiModel[4]; // 3 = 2.5 flash lite, 4 = 2.5 flash
                case OPENAI -> modelSize == LLM_SIZE.SMALL ? openaiModel[0] : openaiModel[1];
                case ANTHROPIC -> modelSize == LLM_SIZE.SMALL ? anthropicModel[0] : anthropicModel[1];
                default -> throw new IllegalArgumentException("Unknown model type: " + modelType);
            };
            if (modelSize == LLM_SIZE.REASONING) {
                if (modelType == LLM_MODEL.OPENAI)
                    modelToUse = openaiModel[2];
                else if (modelType == LLM_MODEL.GEMINI)
                    modelToUse = geminiModel[2];
                else
                    throw new IllegalArgumentException("Reasoning model not available for " + modelType);
            }
            if (modelToUse != null) {
                try {
                    response = modelToUse.chat(query);
                } catch (Exception e) {
                    System.out.println("Error getting response from model: " + e.getMessage());
                }
            } else {
                System.out.println("No valid model available for " + modelType + " " + modelSize);
                return "No reply available";
            }
        }
        // Write to file (if log file is specified)
        if (logWriter != null) {
            String output = String.format("\nModel: %s\nQuery: %s\nResponse: %s\n", modelType, query, response);
            try {
                logWriter.write(output);
                logWriter.flush();
            } catch (Exception e) {
                System.out.println("Error writing to log file: " + e.getMessage());
            }
        }
        outputTokens += tokenizer.estimateTokenCountInText(response);
        return response;
    }

    /**
     * This will use the default LLM model specified in the constructor
     *
     * @param query the prompt sent to the model
     * @return The full text returned by the model; or an empty string if no valid model
     */
    public String getResponse(String query) {
        return getResponse(query, this.modelType, this.modelSize);
    }

    // General implemetation is ready, but model selection is important
    // qwen 3.5 9b, wasn't really the correct model since MLX support is not ready yet
    // it runs on cpu and api call times was worse than gemini 2.0 flash
    private String getResponseWithLocalLLMEndpoints(String query) {
        // hardcoded first model for now, there can be other models to use later on
        String targetModel = localLLMModelNames[1];
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            if (!localLLMModelLoaded) {
                HttpRequest listRequest = HttpRequest.newBuilder()
                        .uri(URI.create(localLLMBaseURL + ":" + localLLMBasePort + "/api/v1/models"))
                        .header("Accept", "application/json")
                        .GET()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();

                String listRespons = client.send(listRequest, HttpResponse.BodyHandlers.ofString()).body();
                JSONObject json = JSONUtils.fromString(listRespons);
                JSONArray models = (JSONArray) json.get("models");

                boolean isLoaded = false;
                for (Object model : models) {
                    JSONObject modelJson = (JSONObject) model;
                    if (targetModel.equals(modelJson.get("key"))) {
                        isLoaded = !((JSONArray) modelJson.get("loaded_instances")).isEmpty();
                        break;
                    }
                }

                if (!isLoaded) {
                    String loadBody = String.format("{\"identifier\": \"%s\"}", targetModel);
                    HttpRequest loadRequest = HttpRequest.newBuilder()
                            .uri(URI.create(localLLMBaseURL + ":" + localLLMBasePort + "/api/v1/models/load"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(loadBody, StandardCharsets.UTF_8))
                            .version(HttpClient.Version.HTTP_1_1)
                            .build();
                    client.send(loadRequest, HttpResponse.BodyHandlers.ofString());
                }
                localLLMModelLoaded = true;
            }

            // TODO : no think from the prompt directly, doesn't work, I turned it of from UI of LMStudio it needs to be fixed
            String chatBody = String.format("{\"model\": \"%s\", \"stream\": false, " +
                            "\"max_tokens\": 200, \"temperature\": 0.3, \"messages\": " +
                            "[{\"role\": \"system\", \"content\": \"/no_think\"}, " +
                            "{\"role\": \"user\", \"content\": %s}]}",
                    targetModel, objectMapper.writeValueAsString(query));

            HttpRequest chatRequest = HttpRequest.newBuilder()
                    .uri(URI.create(localLLMBaseURL + ":" + localLLMBasePort + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(chatBody, StandardCharsets.UTF_8))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            String chatResponse = client.send(chatRequest, HttpResponse.BodyHandlers.ofString()).body();
            JSONObject chatJson = JSONUtils.fromString(chatResponse);
            JSONArray choices = (JSONArray) chatJson.get("choices");
            JSONObject choice = (JSONObject) choices.get(0);
            JSONObject message = (JSONObject) choice.get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            System.out.println("Error getting response from model: " + e.getMessage());
        }

        return "";
    }

    private String getResponseWithOpenRouterHttp(String query) {
        String apiURL = "https://openrouter.ai/api/v1/chat/completions";
        String modelName = "meta-llama/llama-3.1-8b-instruct";

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(query);
        } catch (IOException e) {
            System.out.println("Error converting query to JSON: " + e.getMessage());
            return "Error converting query to JSON";
        }

        String requestBody = String.format("{\"model\":\"%s\",\"max_tokens\":256,\"temperature\":0.1,\"frequency_penalty\":0.3,\"messages\":[{\"role\":\"user\",\"content\":%s}]}", modelName, jsonContent);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL))
                .header("Authorization", "Bearer " + openRouterToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            String rawStringResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            JSONObject json = (rawStringResponse == null || rawStringResponse.isEmpty()) ? null : JSONUtils.fromString(rawStringResponse);
            JSONArray choices = (JSONArray) json.get("choices");

            if (choices == null || choices.isEmpty()) {
                System.out.println("No choices found in response" + rawStringResponse);
                return "";
            }

            JSONObject choice = (JSONObject) choices.get(0);
            JSONObject message = (JSONObject) choice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            System.out.println("Error getting response from model: " + e.getMessage());
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to get response from Gemma model");
    }

    private String getResponseWithLowLevelHttp(String query, LLM_SIZE size) {
        String llamaLocation = size == LLM_SIZE.SMALL ? llamaLocationSmall : llamaLocationLarge;
        String ENDPOINT = llamaLocation + "-aiplatform.googleapis.com";

        // changed from Maverick to 3.3 for better comparability instead of 405B model
        //     String MODEL_NAME = size == LLM_SIZE.SMALL ? "meta/llama-3.1-70b-instruct-maas" : "meta/llama-4-maverick-17b-128e-instruct-maas";
        String MODEL_NAME = size == LLM_SIZE.SMALL ? "meta/llama-3.1-70b-instruct-maas" : "meta/llama-3.3-70b-instruct-maas";
        String apiUrl = String.format("https://%s/v1/projects/%s/locations/%s/endpoints/openapi/chat/completions",
                ENDPOINT, geminiProject, llamaLocation);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonContent;
        try {
            jsonContent = objectMapper.writeValueAsString(query); // Escapes special characters automatically
        } catch (IOException e) {
            System.out.println("Error converting query to JSON: " + e.getMessage());
            return "Error converting query to JSON";
        }
        String requestBody = String.format("{\"model\":\"%s\", \"stream\":false, \"messages\":[{\"role\": \"user\", \"content\": %s}]}",
                MODEL_NAME, jsonContent);

        String ACCESS_TOKEN = getGoogleAccessToken();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            String rawStringResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            if (!rawStringResponse.substring(0, 1).equals("{")) {
                System.out.println("Error in response:");
                System.out.println(rawStringResponse);
            } else {
                JSONObject json = JSONUtils.fromString(rawStringResponse);
                JSONArray choices = (JSONArray) json.get("choices");
                JSONObject choice = (JSONObject) choices.get(0);
                JSONObject message = (JSONObject) choice.get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to get response from Llama model");
    }

    public static void main(String[] args) {
        LLMAccess llm = new LLMAccess(LLM_MODEL.LLAMA, LLM_SIZE.SMALL, "llm_log.txt");
        llm.getResponse("What is the average lifespan of a Spanish Armadillo?");
        llm.getResponse("What is the lifecycle of the European Firefly?", LLM_MODEL.OPENAI, LLM_SIZE.SMALL);
    }

    private static String getGoogleAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
            credentials.refreshIfExpired();
            AccessToken accessToken = credentials.getAccessToken();
            return accessToken.getTokenValue();
        } catch (IOException e) {
            System.out.println("Error getting Google access token: " + e.getMessage());
            throw new RuntimeException("Failed to get Google access token", e);
        }
    }

}
