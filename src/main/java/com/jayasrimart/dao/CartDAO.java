package com.jayasrimart.dao;

import com.jayasrimart.model.CartItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link CartItem} entities.
 */
public interface CartDAO {

    /**
     * Finds all cart items belonging to a user, joined with product details.
     *
     * @param userId the user ID
     * @return list of populated CartItem entities
     */
    List<CartItem> findByUserId(Long userId);

    /**
     * Locates a cart item for a specific user and product combination.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return Optional containing the cart item if present
     */
    Optional<CartItem> findByUserAndProduct(Long userId, Long productId);

    /**
     * Adds an item to the user's cart or increments quantity if already present.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @param quantity quantity to add
     * @return the created or updated CartItem
     */
    CartItem addItem(Long userId, Long productId, int quantity);

    /**
     * Updates the exact quantity of a product in the user's cart.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @param quantity new quantity
     * @return true if updated, false otherwise
     */
    boolean updateQuantity(Long userId, Long productId, int quantity);

    /**
     * Removes a single product from the user's cart.
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if removed, false otherwise
     */
    boolean removeItem(Long userId, Long productId);

    /**
     * Clears all items in a user's cart within an active database transaction.
     *
     * @param userId the user ID
     * @param conn the active transaction connection
     * @return true if cleared successfully
     */
    boolean clearCart(Long userId, Connection conn);

    /**
     * Clears all items in a user's cart outside a transaction.
     *
     * @param userId the user ID
     * @return true if cleared successfully
     */
    boolean clearCart(Long userId);

    /**
     * Counts the total number of items in a user's cart (sum of quantities).
     *
     * @param userId the user ID
     * @return total count of items
     */
    int countItemsByUserId(Long userId);
}
