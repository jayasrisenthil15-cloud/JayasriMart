package com.jayasrimart.service;

import com.jayasrimart.model.Product;

import java.util.List;

/**
 * Service interface managing buyer wishlist interactions and personalized product recommendations.
 */
public interface WishlistService {

    /**
     * Toggles the wishlist status of a product for a given user.
     * If the item is currently saved, it is removed; otherwise, it is added.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if the product is now in the wishlist, false if removed
     */
    boolean toggleWishlist(Long userId, Long productId);

    /**
     * Removes a product from a user's wishlist.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if removed, false otherwise
     */
    boolean removeFromWishlist(Long userId, Long productId);

    /**
     * Checks whether a specific product is saved in the user's wishlist.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if in wishlist, false otherwise
     */
    boolean isInWishlist(Long userId, Long productId);

    /**
     * Retrieves all products currently saved in the user's wishlist.
     *
     * @param userId the user ID
     * @return list of wishlisted products
     */
    List<Product> getWishlist(Long userId);

    /**
     * Retrieves the list of wishlisted product IDs for fast O(1) in-memory checks in JSP views.
     *
     * @param userId the user ID
     * @return list of product IDs
     */
    List<Long> getWishlistProductIds(Long userId);

    /**
     * Counts the total items saved in the user's wishlist.
     *
     * @param userId the user ID
     * @return count of saved products
     */
    int getWishlistCount(Long userId);

    /**
     * Generates personalized product recommendations based on categories the buyer has wishlisted or purchased.
     * Falls back to top-rated featured products if no prior history exists.
     *
     * @param userId the user ID (optional, can be null for guest visitors)
     * @param limit maximum recommendations to return
     * @return list of recommended products
     */
    List<Product> getRecommendations(Long userId, int limit);
}
