package com.jayasrimart.dao;

import com.jayasrimart.model.Product;

import java.util.List;

/**
 * Data Access Object interface for managing saved {@link com.jayasrimart.model.WishlistItem} records.
 */
public interface WishlistDAO {

    /**
     * Adds a product to a buyer's wishlist.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if added, false if already exists
     */
    boolean add(Long userId, Long productId);

    /**
     * Removes a product from a buyer's wishlist.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if removed, false otherwise
     */
    boolean remove(Long userId, Long productId);

    /**
     * Checks if a product exists in a buyer's wishlist.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if wishlisted, false otherwise
     */
    boolean exists(Long userId, Long productId);

    /**
     * Retrieves all product entities currently wishlisted by the buyer.
     *
     * @param userId the user ID
     * @return list of wishlisted products
     */
    List<Product> findWishlistProducts(Long userId);

    /**
     * Retrieves a list of all product IDs currently wishlisted by the buyer.
     *
     * @param userId the user ID
     * @return list of product IDs
     */
    List<Long> findProductIdsByUserId(Long userId);

    /**
     * Counts the total number of items saved in a buyer's wishlist.
     *
     * @param userId the user ID
     * @return total wishlisted count
     */
    int countByUserId(Long userId);

    /**
     * Discovers distinct product categories interacted with by the buyer (from wishlist and past orders)
     * to drive smart recommendation analytics.
     *
     * @param userId the user ID
     * @return list of category names
     */
    List<String> findInteractedCategories(Long userId);
}
