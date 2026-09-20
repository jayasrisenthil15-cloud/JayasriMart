package com.jayasrimart.service;

import com.jayasrimart.dto.ReviewRequestDTO;
import com.jayasrimart.model.Review;

import java.util.List;

/**
 * Service interface managing customer reviews, star rating submissions, eligibility checks,
 * and automated product average rating calculations.
 */
public interface ReviewService {

    /**
     * Submits a customer review for a product purchased in a delivered order,
     * recalculates the product's average rating, and updates the product in ONE transaction.
     *
     * @param userId authenticated buyer ID
     * @param request review payload containing productId, orderId, rating (1-5), and comment
     * @return persisted {@link Review}
     */
    Review submitReview(Long userId, ReviewRequestDTO request);

    /**
     * Retrieves all customer reviews for a given product.
     *
     * @param productId the product ID
     * @return list of reviews
     */
    List<Review> getProductReviews(Long productId);

    /**
     * Checks whether a buyer is eligible to write a review for a specific product and order.
     * Eligibility requires:
     * 1. Order exists and belongs to the buyer
     * 2. Order status is DELIVERED
     * 3. Order contains the product
     * 4. No previous review exists for this (user, product, order) combination.
     *
     * @param userId buyer user ID
     * @param productId product ID
     * @param orderId order ID
     * @return true if eligible to review, false otherwise
     */
    boolean isEligibleForReview(Long userId, Long productId, Long orderId);
}
