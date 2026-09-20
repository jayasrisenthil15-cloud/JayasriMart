package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.ReviewDAO;
import com.jayasrimart.dto.ReviewRequestDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.exception.DuplicateResourceException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Review;
import com.jayasrimart.service.ReviewService;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link ReviewService}.
 * Enforces review eligibility rules and atomic product average rating updates within transactions.
 */
public class ReviewServiceImpl implements ReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewDAO reviewDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public ReviewServiceImpl() {
        this(DaoFactory.getReviewDAO(), DaoFactory.getOrderDAO(), DaoFactory.getProductDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param reviewDAO the review DAO interface
     * @param orderDAO the order DAO interface
     * @param productDAO the product DAO interface
     */
    public ReviewServiceImpl(ReviewDAO reviewDAO, OrderDAO orderDAO, ProductDAO productDAO) {
        if (reviewDAO == null) {
            throw new IllegalArgumentException("ReviewDAO dependency cannot be null");
        }
        if (orderDAO == null) {
            throw new IllegalArgumentException("OrderDAO dependency cannot be null");
        }
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO dependency cannot be null");
        }
        this.reviewDAO = reviewDAO;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    @Override
    public Review submitReview(Long userId, ReviewRequestDTO request) {
        // 1. Validate inputs at top of service method
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (request == null) {
            throw new ValidationException("request", "Review request payload cannot be null.");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ValidationException("rating", "Star rating must be between 1 and 5.");
        }

        String comment = ValidationUtil.requireNonBlank(request.getComment(), "comment");
        if (comment.length() < 5 || comment.length() > 1000) {
            throw new ValidationException("comment", "Review comment must be between 5 and 1000 characters.");
        }

        Long productId = request.getProductId();
        Long orderId = request.getOrderId();

        // 2. Verify Product existence
        productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // 3. Verify Order & Eligibility Rules
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!userId.equals(order.getBuyerId())) {
            LOGGER.warn("Unauthorized review attempt: User {} tried reviewing order {} owned by User {}",
                    userId, orderId, order.getBuyerId());
            throw new UnauthorizedException("You can only review products from your own orders.");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ValidationException("orderStatus",
                    "Reviews are only permitted for delivered orders. Current order status: " + order.getStatus());
        }

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(productId));
        if (!productInOrder) {
            throw new ValidationException("product", "This product was not purchased in the specified order.");
        }

        if (reviewDAO.findByUserProductOrder(userId, productId, orderId).isPresent()) {
            throw new DuplicateResourceException("You have already submitted a review for this product in order #ORD-" + orderId + ".");
        }

        // 4. Atomic Database Transaction: Insert Review + Recalculate & Update Product Average Rating
        Review newReview = new Review();
        newReview.setUserId(userId);
        newReview.setProductId(productId);
        newReview.setOrderId(orderId);
        newReview.setRating(request.getRating());
        newReview.setComment(comment.trim());

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Review createdReview = reviewDAO.create(newReview, conn);

                BigDecimal newAvgRating = reviewDAO.calculateAverageRating(productId, conn);
                productDAO.updateAvgRating(productId, newAvgRating, conn);

                conn.commit();
                LOGGER.info("User {} published review for product ID {} with rating {}. New average rating: {}",
                        userId, productId, request.getRating(), newAvgRating);

                return createdReview;
            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Failed to submit review for user {} product {}: {}", userId, productId, e.getMessage(), e);
                if (e instanceof AppException appException) {
                    throw appException;
                }
                throw new AppException("Failed to submit customer review.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database error submitting review for product {}: {}", productId, e.getMessage(), e);
            throw new AppException("Database connection error during review submission.", e);
        }
    }

    @Override
    public List<Review> getProductReviews(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }
        return reviewDAO.findByProductId(productId);
    }

    @Override
    public boolean isEligibleForReview(Long userId, Long productId, Long orderId) {
        if (userId == null || productId == null || orderId == null) {
            return false;
        }
        try {
            Order order = orderDAO.findById(orderId).orElse(null);
            if (order == null || !userId.equals(order.getBuyerId()) || order.getStatus() != OrderStatus.DELIVERED) {
                return false;
            }
            boolean itemInOrder = order.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
            if (!itemInOrder) {
                return false;
            }
            return reviewDAO.findByUserProductOrder(userId, productId, orderId).isEmpty();
        } catch (Exception e) {
            LOGGER.error("Error checking review eligibility for user {} product {} order {}: {}",
                    userId, productId, orderId, e.getMessage(), e);
            return false;
        }
    }
}
