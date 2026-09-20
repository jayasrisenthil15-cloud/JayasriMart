package com.jayasrimart.controller;

import com.jayasrimart.dto.ApiResponse;
import com.jayasrimart.dto.ChatRequestDTO;
import com.jayasrimart.dto.ChatResponseDTO;
import com.jayasrimart.service.ChatbotService;
import com.jayasrimart.service.impl.ChatbotServiceImpl;
import com.jayasrimart.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * REST endpoint providing AI-powered shopping assistance, product discovery, and store guidance.
 */
@WebServlet(name = "ChatServlet", urlPatterns = {"/api/v1/chat", "/chat"})
public class ChatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServlet.class);

    private final ChatbotService chatbotService;

    /**
     * Default constructor.
     */
    public ChatServlet() {
        this(new ChatbotServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param chatbotService the chatbot service interface
     */
    public ChatServlet(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            ChatRequestDTO requestDTO;
            String contentType = req.getContentType();

            if (contentType != null && contentType.contains("application/json")) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                requestDTO = JsonUtil.fromJson(sb.toString(), ChatRequestDTO.class);
            } else {
                String message = req.getParameter("message");
                requestDTO = new ChatRequestDTO(message);
            }

            if (requestDTO == null || requestDTO.getMessage() == null) {
                requestDTO = new ChatRequestDTO("");
            }

            ChatResponseDTO responseDTO = chatbotService.processChat(requestDTO);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(responseDTO)));

        } catch (Exception e) {
            LOGGER.error("Error processing AI chat message: {}", e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.toJson(
                    ApiResponse.error("CHAT_ERROR", "Failed to process shopping assistant query.")));
        }
    }
}
