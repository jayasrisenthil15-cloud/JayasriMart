package com.jayasrimart.service;

import com.jayasrimart.dto.CartResponseDTO;

/**
 * Service interface managing shopping cart operations, calculations, and stock checks.
 */
public interface CartService {

    /**
     * Retrieves the complete shopping cart summary for a buyer.
     *
     * @param userId the buyer user ID
     * @return populated {@link CartResponseDTO}
     */
    CartResponseDTO getCart(Long userId);

    /**
     * Adds an item to the buyer's cart, verifying stock availability.
     *
     * @param userId the buyer user ID
     * @param productId the product ID
     * @param quantity quantity to add
     * @return updated {@link CartResponseDTO}
     */
    CartResponseDTO addToCart(Long userId, Long productId, int quantity);

    /**
     * Updates the exact quantity of an item in the cart.
     *
     * @param userId the buyer user ID
     * @param productId the product ID
     * @param quantity target quantity
     * @return updated {@link CartResponseDTO}
     */
    CartResponseDTO updateCartItem(Long userId, Long productId, int quantity);

    /**
     * Removes an item completely from the buyer's cart.
     *
     * @param userId the buyer user ID
     * @param productId the product ID
     * @return updated {@link CartResponseDTO}
     */
    CartResponseDTO removeFromCart(Long userId, Long productId);

    /**
     * Clears all items in a buyer's cart.
     *
     * @param userId the buyer user ID
     */
    void clearCart(Long userId);

    /**
     * Gets the total count of items in the cart for navbar badge display.
     *
     * @param userId the buyer user ID
     * @return count of items
     */
    int getCartCount(Long userId);
}
