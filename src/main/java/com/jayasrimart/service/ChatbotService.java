package com.jayasrimart.service;

import com.jayasrimart.dto.ChatRequestDTO;
import com.jayasrimart.dto.ChatResponseDTO;

/**
 * Service interface orchestrating AI chatbot interactions and catalog context retrieval.
 */
public interface ChatbotService {

    /**
     * Processes a user chat message, matches product catalog context, and generates an AI shopping response.
     *
     * @param request the chat request payload
     * @return structured chat response with reply text and suggested product cards
     */
    ChatResponseDTO processChat(ChatRequestDTO request);
}
