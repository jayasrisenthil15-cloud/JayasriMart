package com.jayasrimart.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Entity representing an item saved in a buyer's personal wishlist.
 */
public class WishlistItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long productId;
    private Timestamp createdAt;

    // Transient joined product entity
    private Product product;

    /**
     * Default constructor.
     */
    public WishlistItem() {
    }

    /**
     * Parameterized constructor.
     *
     * @param userId the buyer user ID
     * @param productId the product ID
     */
    public WishlistItem(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
