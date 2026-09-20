package com.jayasrimart.dto;

import com.jayasrimart.model.Product;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object containing the assistant reply and matched product recommendations.
 */
public class ChatResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reply;
    private List<Product> suggestedProducts;

    public ChatResponseDTO() {
        this.suggestedProducts = Collections.emptyList();
    }

    public ChatResponseDTO(String reply) {
        this.reply = reply;
        this.suggestedProducts = Collections.emptyList();
    }

    public ChatResponseDTO(String reply, List<Product> suggestedProducts) {
        this.reply = reply;
        this.suggestedProducts = suggestedProducts != null ? suggestedProducts : Collections.emptyList();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<Product> getSuggestedProducts() {
        return suggestedProducts;
    }

    public void setSuggestedProducts(List<Product> suggestedProducts) {
        this.suggestedProducts = suggestedProducts;
    }
}
