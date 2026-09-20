package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.WishlistDAO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Product;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link WishlistDAO}.
 */
public class WishlistDaoImpl implements WishlistDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(WishlistDaoImpl.class);

    private static final String SQL_ADD =
            "INSERT INTO wishlist (user_id, product_id, created_at) VALUES (?, ?, ?)";

    private static final String SQL_REMOVE =
            "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";

    private static final String SQL_EXISTS =
            "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";

    private static final String SQL_FIND_PRODUCTS =
            "SELECT p.id, p.seller_id, p.name, p.description, p.price, p.stock_qty, p.category, p.image_url, "
            + "p.avg_rating, p.active, p.created_at, u.name AS seller_name "
            + "FROM wishlist w "
            + "JOIN products p ON w.product_id = p.id "
            + "JOIN users u ON p.seller_id = u.id "
            + "WHERE w.user_id = ? "
            + "ORDER BY w.created_at DESC";

    private static final String SQL_FIND_PRODUCT_IDS =
            "SELECT product_id FROM wishlist WHERE user_id = ? ORDER BY created_at DESC";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) AS total FROM wishlist WHERE user_id = ?";

    private static final String SQL_INTERACTED_CATEGORIES =
            "SELECT DISTINCT p.category FROM products p WHERE p.id IN ("
            + "SELECT product_id FROM wishlist WHERE user_id = ? "
            + "UNION "
            + "SELECT oi.product_id FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE o.buyer_id = ?)";

    @Override
    public boolean add(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        if (exists(userId, productId)) {
            return true;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ADD)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setTimestamp(3, now);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error adding product {} to wishlist for user {}: {}",
                    productId, userId, e.getMessage(), e);
            throw new AppException("Failed to add product to wishlist", e);
        }
    }

    @Override
    public boolean remove(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_REMOVE)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error removing product {} from wishlist for user {}: {}",
                    productId, userId, e.getMessage(), e);
            throw new AppException("Failed to remove product from wishlist", e);
        }
    }

    @Override
    public boolean exists(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.error("Database error checking wishlist existence for user {} product {}: {}",
                    userId, productId, e.getMessage(), e);
            throw new AppException("Failed to check wishlist status", e);
        }
    }

    @Override
    public List<Product> findWishlistProducts(Long userId) {
        List<Product> products = new ArrayList<>();
        if (userId == null) {
            return products;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_PRODUCTS)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProductRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error finding wishlist products for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to retrieve wishlist products", e);
        }
        return products;
    }

    @Override
    public List<Long> findProductIdsByUserId(Long userId) {
        List<Long> ids = new ArrayList<>();
        if (userId == null) {
            return ids;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_PRODUCT_IDS)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("product_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error finding wishlist product IDs for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to retrieve wishlist IDs", e);
        }
        return ids;
    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error counting wishlist for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to count wishlist items", e);
        }
        return 0;
    }

    @Override
    public List<String> findInteractedCategories(Long userId) {
        List<String> categories = new ArrayList<>();
        if (userId == null) {
            return categories;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INTERACTED_CATEGORIES)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cat = rs.getString("category");
                    if (cat != null && !cat.trim().isEmpty()) {
                        categories.add(cat.trim());
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error discovering interacted categories for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to analyze buyer category preferences", e);
        }
        return categories;
    }

    private Product mapProductRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setAvgRating(rs.getBigDecimal("avg_rating"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setSellerName(rs.getString("seller_name"));
        return p;
    }
}
