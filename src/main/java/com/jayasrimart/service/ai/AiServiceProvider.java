package com.jayasrimart.service.ai;

import com.jayasrimart.dto.ChatMessageDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.model.Product;

import java.util.List;

/**
 * Strategy interface defining AI-driven conversational shopping assistance providers.
 */
public interface AiServiceProvider {

    /**
     * Generates a conversational response and product recommendations given the user prompt and catalog context.
     *
     * @param userMessage the user's message
     * @param history prior conversation turns (optional)
     * @param catalogContext matched or featured product catalog items for ground-truth context
     * @return structured chat response with reply text and suggested product cards
     */
    ChatResponseDTO generateResponse(String userMessage, List<ChatMessageDTO> history, List<Product> catalogContext);

    /**
     * Checks whether this AI provider is properly configured and operational.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}
