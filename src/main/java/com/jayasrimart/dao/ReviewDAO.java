package com.jayasrimart.dao;

import com.jayasrimart.model.Review;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link Review} entities.
 */
public interface ReviewDAO {

    /**
     * Finds all customer reviews for a given product ID, joined with user name.
     *
     * @param productId the product ID
     * @return list of reviews ordered by created date descending
     */
    List<Review> findByProductId(Long productId);

    /**
     * Checks if a review already exists for a specific user, product, and delivered order.
     *
     * @param userId the buyer user ID
     * @param productId the product ID
     * @param orderId the delivered order ID
     * @return Optional containing the review if found
     */
    Optional<Review> findByUserProductOrder(Long userId, Long productId, Long orderId);

    /**
     * Inserts a new review within an active database connection transaction.
     *
     * @param review the review entity
     * @param conn the active SQL connection
     * @return the created review with generated ID
     */
    Review create(Review review, Connection conn);

    /**
     * Computes the average star rating for a product from the reviews table.
     *
     * @param productId the product ID
     * @param conn the active SQL connection
     * @return the average rating as {@link BigDecimal}, or 0.00 if no reviews
     */
    BigDecimal calculateAverageRating(Long productId, Connection conn);

    /**
     * Counts the total number of reviews for a product.
     *
     * @param productId the product ID
     * @return total review count
     */
    int countByProductId(Long productId);
}
