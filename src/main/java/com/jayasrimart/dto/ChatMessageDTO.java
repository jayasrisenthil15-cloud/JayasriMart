package com.jayasrimart.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing a single chat conversation turn.
 */
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String role;
    private String content;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
