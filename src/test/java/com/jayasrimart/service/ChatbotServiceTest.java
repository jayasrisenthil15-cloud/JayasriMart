package com.jayasrimart.service;

import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.ChatMessageDTO;
import com.jayasrimart.dto.ChatRequestDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.ai.AiServiceFactory;
import com.jayasrimart.service.ai.AiServiceProvider;
import com.jayasrimart.service.ai.MockAiServiceProvider;
import com.jayasrimart.service.impl.ChatbotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChatbotService} and {@link MockAiServiceProvider}.
 */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ProductDAO productDAO;

    private AiServiceProvider mockAiProvider;
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        mockAiProvider = new MockAiServiceProvider();
        chatbotService = new ChatbotServiceImpl(productDAO, mockAiProvider);
    }

    @Test
    void testProcessChat_NullRequest_ThrowsException() {
        assertThrows(ValidationException.class, () -> chatbotService.processChat(null));
    }

    @Test
    void testProcessChat_EmptyMessage_ReturnsDefaultGreeting() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("   ");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("JayaBot"));
    }

    @Test
    void testProcessChat_ShippingPolicy() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("What is your delivery fee and shipping policy?");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Shipping Policy"));
        assertTrue(response.getReply().contains("Free Delivery"));
    }

    @Test
    void testProcessChat_PaymentMethods() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Can I pay with UPI or Cash on delivery?");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Payment Methods"));
        assertTrue(response.getReply().contains("UPI"));
        assertTrue(response.getReply().contains("Cash on Delivery"));
    }

    @Test
    void testProcessChat_ReturnsAndCancellation() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("How do I request a return or cancel my order?");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Returns & Cancellation"));
    }

    @Test
    void testProcessChat_SellerInquiry() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("I want to register as a seller vendor");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Want to sell on JayasriMart?"));
    }

    @Test
    void testProcessChat_GeneralGreeting() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Hello");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("JayaBot"));
    }

    @Test
    void testProcessChat_ProductRecommendations() {
        Product p1 = new Product();
        p1.setId(10L);
        p1.setName("Wireless Noise-Cancelling Headphones");
        p1.setPrice(new BigDecimal("2499.00"));
        p1.setAvgRating(new BigDecimal("4.8"));

        when(productDAO.searchAndFilter(any(ProductSearchCriteria.class)))
                .thenReturn(List.of(p1));

        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Find headphones for study");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertNotNull(response.getSuggestedProducts());
        assertFalse(response.getSuggestedProducts().isEmpty());
        assertEquals("Wireless Noise-Cancelling Headphones", response.getSuggestedProducts().get(0).getName());
        assertTrue(response.getReply().contains("Headphones"));
    }

    @Test
    void testProcessChat_FallbackFeaturedWhenNoKeywordsMatch() {
        Product featured = new Product();
        featured.setId(20L);
        featured.setName("Smart Fitness Watch Pro");
        featured.setPrice(new BigDecimal("1999.00"));
        featured.setAvgRating(new BigDecimal("4.5"));

        when(productDAO.searchAndFilter(any(ProductSearchCriteria.class)))
                .thenReturn(Collections.emptyList());
        when(productDAO.findFeatured(3))
                .thenReturn(List.of(featured));

        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("xyzabc random nonsense query");
        ChatResponseDTO response = chatbotService.processChat(request);

        assertNotNull(response);
        assertNotNull(response.getSuggestedProducts());
        assertEquals(1, response.getSuggestedProducts().size());
        assertEquals("Smart Fitness Watch Pro", response.getSuggestedProducts().get(0).getName());
    }

    @Test
    void testMockAiServiceProvider_IsAvailable() {
        assertTrue(mockAiProvider.isAvailable());
    }

    @Test
    void testAiServiceFactory_GetProvider() {
        AiServiceProvider provider = AiServiceFactory.getProvider();
        assertNotNull(provider);
        assertTrue(provider.isAvailable());
    }

    @Test
    void testChatMessageDTO_GettersAndSetters() {
        ChatMessageDTO msg = new ChatMessageDTO("user", "Hello assistant");
        assertEquals("user", msg.getRole());
        assertEquals("Hello assistant", msg.getContent());

        msg.setRole("assistant");
        msg.setContent("Hello user");
        assertEquals("assistant", msg.getRole());
        assertEquals("Hello user", msg.getContent());
    }
}
