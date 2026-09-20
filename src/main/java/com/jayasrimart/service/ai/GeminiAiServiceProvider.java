package com.jayasrimart.service.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayasrimart.dto.ChatMessageDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * AI Provider integrating with Google Gemini API for real-time generative shopping assistance.
 * Automatically falls back to {@link MockAiServiceProvider} when API key is missing or on network timeouts.
 */
public class GeminiAiServiceProvider implements AiServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiAiServiceProvider.class);
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + DEFAULT_MODEL + ":generateContent?key=";

    private final String apiKey;
    private final HttpClient httpClient;
    private final MockAiServiceProvider mockFallback;
    private final Gson gson;

    /**
     * Default constructor reading API key from environment or system properties.
     */
    public GeminiAiServiceProvider() {
        this(resolveApiKey(), new MockAiServiceProvider());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param apiKey the Gemini API key
     * @param mockFallback fallback provider
     */
    public GeminiAiServiceProvider(String apiKey, MockAiServiceProvider mockFallback) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.mockFallback = mockFallback != null ? mockFallback : new MockAiServiceProvider();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
        this.gson = new Gson();
    }

    private static String resolveApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = System.getProperty("gemini.api.key");
        }
        return key != null ? key.trim() : "";
    }

    @Override
    public boolean isAvailable() {
        return !apiKey.isEmpty();
    }

    @Override
    public ChatResponseDTO generateResponse(String userMessage, List<ChatMessageDTO> history, List<Product> catalogContext) {
        if (!isAvailable()) {
            LOGGER.debug("Gemini API key not configured. Using MockAiServiceProvider fallback.");
            return mockFallback.generateResponse(userMessage, history, catalogContext);
        }

        try {
            String prompt = buildPrompt(userMessage, catalogContext);
            String requestJson = createRequestBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_ENDPOINT + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String replyText = parseGeminiResponse(response.body());
                if (replyText != null && !replyText.trim().isEmpty()) {
                    return new ChatResponseDTO(replyText.trim(), catalogContext);
                }
            }

            LOGGER.warn("Gemini API returned status code {}: {}. Falling back to rule-based assistant.",
                    response.statusCode(), response.body());
            return mockFallback.generateResponse(userMessage, history, catalogContext);

        } catch (Exception e) {
            LOGGER.warn("Gemini API invocation failed ({}). Falling back to rule-based assistant.", e.getMessage());
            return mockFallback.generateResponse(userMessage, history, catalogContext);
        }
    }

    private String buildPrompt(String userMessage, List<Product> catalog) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are JayaBot, the friendly and intelligent AI shopping assistant for JayasriMart, ")
          .append("a multi-seller e-commerce marketplace in India.\n")
          .append("Key Store Policies:\n")
          .append("- Free Delivery on orders >= ₹999.00; Flat ₹50.00 delivery fee on orders below ₹999.00.\n")
          .append("- Payment methods: UPI (instant mock validation), Credit/Debit Cards, Cash on Delivery (COD).\n")
          .append("- Cancellation permitted anytime before order is SHIPPED.\n")
          .append("- Multi-vendor marketplace where buyers can purchase from various verified sellers.\n\n");

        if (catalog != null && !catalog.isEmpty()) {
            sb.append("Current Catalog Products for Grounding Context:\n");
            for (Product p : catalog) {
                sb.append("- [ID: ").append(p.getId()).append("] ")
                  .append(p.getName()).append(" | Category: ").append(p.getCategory())
                  .append(" | Price: ₹").append(p.getPrice())
                  .append(" | Rating: ").append(p.getAvgRating())
                  .append(" | Stock: ").append(p.getStockQty()).append(" units\n");
            }
            sb.append("\n");
        }

        sb.append("User Query: ").append(userMessage).append("\n")
          .append("Instructions: Provide a concise, helpful, and courteous answer. Mention relevant products and prices if applicable.");

        return sb.toString();
    }

    private String createRequestBody(String prompt) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject root = new JsonObject();
        root.add("contents", contents);

        return gson.toJson(root);
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                if (content != null) {
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse Gemini response JSON: {}", e.getMessage(), e);
        }
        return null;
    }
}
