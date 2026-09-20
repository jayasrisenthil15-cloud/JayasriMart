package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.ReviewDAO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Review;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link ReviewDAO} using parameterized statements.
 */
public class ReviewDaoImpl implements ReviewDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDaoImpl.class);

    private static final String SQL_FIND_BY_PRODUCT_ID =
            "SELECT r.id, r.user_id, r.product_id, r.order_id, r.rating, r.comment, r.created_at, u.name AS user_name "
            + "FROM reviews r "
            + "JOIN users u ON r.user_id = u.id "
            + "WHERE r.product_id = ? "
            + "ORDER BY r.created_at DESC";

    private static final String SQL_FIND_BY_USER_PRODUCT_ORDER =
            "SELECT r.id, r.user_id, r.product_id, r.order_id, r.rating, r.comment, r.created_at, u.name AS user_name "
            + "FROM reviews r "
            + "JOIN users u ON r.user_id = u.id "
            + "WHERE r.user_id = ? AND r.product_id = ? AND r.order_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO reviews (user_id, product_id, order_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_CALC_AVG_RATING =
            "SELECT COALESCE(AVG(CAST(rating AS DECIMAL(5,2))), 0.00) AS avg_rating FROM reviews WHERE product_id = ?";

    private static final String SQL_COUNT_BY_PRODUCT_ID =
            "SELECT COUNT(*) AS total FROM reviews WHERE product_id = ?";

    @Override
    public List<Review> findByProductId(Long productId) {
        List<Review> reviews = new ArrayList<>();
        if (productId == null) {
            return reviews;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PRODUCT_ID)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error querying reviews for productId {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to retrieve reviews", e);
        }
        return reviews;
    }

    @Override
    public Optional<Review> findByUserProductOrder(Long userId, Long productId, Long orderId) {
        if (userId == null || productId == null || orderId == null) {
            return Optional.empty();
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_PRODUCT_ORDER)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setLong(3, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking review eligibility for user {} product {} order {}: {}",
                    userId, productId, orderId, e.getMessage(), e);
            throw new AppException("Failed to check review status", e);
        }
        return Optional.empty();
    }

    @Override
    public Review create(Review review, Connection conn) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getUserId());
            ps.setLong(2, review.getProductId());
            ps.setLong(3, review.getOrderId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.setTimestamp(6, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AppException("Failed to insert review: no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getLong(1));
                    review.setCreatedAt(now);
                    return review;
                } else {
                    throw new AppException("Failed to retrieve generated ID for review.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error creating review for product {}: {}", review.getProductId(), e.getMessage(), e);
            throw new AppException("Failed to save review in database", e);
        }
    }

    @Override
    public BigDecimal calculateAverageRating(Long productId, Connection conn) {
        if (productId == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_CALC_AVG_RATING)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal avg = rs.getBigDecimal("avg_rating");
                    return (avg != null) ? avg.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error calculating average rating for product {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to compute product average rating", e);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int countByProductId(Long productId) {
        if (productId == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_PRODUCT_ID)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting reviews for productId {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to count reviews", e);
        }
        return 0;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setUserId(rs.getLong("user_id"));
        review.setProductId(rs.getLong("product_id"));
        review.setOrderId(rs.getLong("order_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            review.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {
            // Column may not be in all projections
        }
        return review;
    }
}
