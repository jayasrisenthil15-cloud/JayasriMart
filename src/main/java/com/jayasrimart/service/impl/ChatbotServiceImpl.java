package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.ChatRequestDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.ChatbotService;
import com.jayasrimart.service.ai.AiServiceFactory;
import com.jayasrimart.service.ai.AiServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ChatbotService}.
 * Gathers relevant product catalog context from {@link ProductDAO} and delegates to {@link AiServiceProvider}.
 */
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private final ProductDAO productDAO;
    private final AiServiceProvider aiServiceProvider;

    /**
     * Default constructor utilizing factory singletons.
     */
    public ChatbotServiceImpl() {
        this(DaoFactory.getProductDAO(), AiServiceFactory.getProvider());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param productDAO the product DAO
     * @param aiServiceProvider the AI provider
     */
    public ChatbotServiceImpl(ProductDAO productDAO, AiServiceProvider aiServiceProvider) {
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO cannot be null");
        }
        if (aiServiceProvider == null) {
            throw new IllegalArgumentException("AiServiceProvider cannot be null");
        }
        this.productDAO = productDAO;
        this.aiServiceProvider = aiServiceProvider;
    }

    @Override
    public ChatResponseDTO processChat(ChatRequestDTO request) {
        if (request == null) {
            throw new ValidationException("request", "Chat request cannot be null.");
        }

        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isEmpty()) {
            return new ChatResponseDTO("Hello! I am JayaBot, your AI shopping assistant. How can I help you today?");
        }

        List<Product> catalogContext = retrieveCatalogContext(userMessage);

        LOGGER.debug("Processing chat query '{}' with {} contextual products", userMessage, catalogContext.size());
        return aiServiceProvider.generateResponse(userMessage, request.getHistory(), catalogContext);
    }

    private List<Product> retrieveCatalogContext(String query) {
        List<Product> matched = new ArrayList<>();
        try {
            ProductSearchCriteria criteria = new ProductSearchCriteria();
            criteria.setKeyword(query);
            criteria.setActiveOnly(true);
            criteria.setLimit(4);

            matched = productDAO.searchAndFilter(criteria);

            // If no exact match on the full phrase, try individual keywords
            if (matched.isEmpty()) {
                String[] words = query.split("\\s+");
                for (String word : words) {
                    String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
                    if (cleanWord.length() > 3) {
                        criteria.setKeyword(cleanWord);
                        matched = productDAO.searchAndFilter(criteria);
                        if (!matched.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            // If still empty, supply top featured items as general context
            if (matched.isEmpty()) {
                matched = productDAO.findFeatured(3);
            }
        } catch (Exception e) {
            LOGGER.error("Error searching catalog context for chat query '{}': {}", query, e.getMessage(), e);
        }
        return matched;
    }
}
