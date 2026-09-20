package com.jayasrimart.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object for incoming chat requests from the user interface.
 */
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private List<ChatMessageDTO> history;

    public ChatRequestDTO() {
        this.history = Collections.emptyList();
    }

    public ChatRequestDTO(String message) {
        this.message = message;
        this.history = Collections.emptyList();
    }

    public ChatRequestDTO(String message, List<ChatMessageDTO> history) {
        this.message = message;
        this.history = history != null ? history : Collections.emptyList();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDTO> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDTO> history) {
        this.history = history;
    }
}
