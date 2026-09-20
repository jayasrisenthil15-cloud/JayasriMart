package com.jayasrimart.dto;

import java.io.Serializable;

/**
 * Data Transfer Object representing a customer review submission payload.
 */
public class ReviewRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    private Long orderId;
    private int rating;
    private String comment;

    public ReviewRequestDTO() {
    }

    public ReviewRequestDTO(Long productId, Long orderId, int rating, String comment) {
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
