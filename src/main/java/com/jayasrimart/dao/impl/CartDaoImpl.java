package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.CartDAO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.CartItem;
import com.jayasrimart.model.Product;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * JDBC implementation of {@link CartDAO}.
 */
public class CartDaoImpl implements CartDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartDaoImpl.class);

    private static final String SQL_FIND_BY_USER_ID =
            "SELECT c.id, c.user_id, c.product_id, c.quantity, c.created_at, "
            + "p.seller_id, p.name AS product_name, p.price AS product_price, p.stock_qty, "
            + "p.image_url, p.category, p.avg_rating, p.active, u.name AS seller_name "
            + "FROM cart_items c "
            + "JOIN products p ON c.product_id = p.id "
            + "JOIN users u ON p.seller_id = u.id "
            + "WHERE c.user_id = ? "
            + "ORDER BY c.created_at ASC";

    private static final String SQL_FIND_BY_USER_AND_PRODUCT =
            "SELECT id, user_id, product_id, quantity, created_at FROM cart_items WHERE user_id = ? AND product_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO cart_items (user_id, product_id, quantity, created_at) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_QTY =
            "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";

    private static final String SQL_DELETE_ITEM =
            "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";

    private static final String SQL_CLEAR_CART =
            "DELETE FROM cart_items WHERE user_id = ?";

    private static final String SQL_COUNT_ITEMS =
            "SELECT COALESCE(SUM(quantity), 0) AS total FROM cart_items WHERE user_id = ?";

    @Override
    public List<CartItem> findByUserId(Long userId) {
        List<CartItem> items = new ArrayList<>();
        if (userId == null) {
            return items;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_ID)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowWithProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error finding cart items for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to retrieve cart items", e);
        }
        return items;
    }

    @Override
    public Optional<CartItem> findByUserAndProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return Optional.empty();
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER_AND_PRODUCT)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    return Optional.of(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findByUserAndProduct for user {} product {}: {}",
                    userId, productId, e.getMessage(), e);
            throw new AppException("Failed to query cart item", e);
        }
        return Optional.empty();
    }

    @Override
    public CartItem addItem(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid cart item parameters.");
        }

        Optional<CartItem> existing = findByUserAndProduct(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + quantity;
            updateQuantity(userId, productId, newQty);
            item.setQuantity(newQty);
            return item;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.setTimestamp(4, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AppException("Failed to insert cart item: no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    CartItem newItem = new CartItem();
                    newItem.setId(generatedKeys.getLong(1));
                    newItem.setUserId(userId);
                    newItem.setProductId(productId);
                    newItem.setQuantity(quantity);
                    newItem.setCreatedAt(now);
                    return newItem;
                } else {
                    throw new AppException("Failed to retrieve generated ID for cart item.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error adding item to cart for user {} product {}: {}", userId, productId, e.getMessage(), e);
            throw new AppException("Failed to add item to cart", e);
        }
    }

    @Override
    public boolean updateQuantity(Long userId, Long productId, int quantity) {
        if (userId == null || productId == null || quantity <= 0) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_QTY)) {
            ps.setInt(1, quantity);
            ps.setLong(2, userId);
            ps.setLong(3, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating cart quantity for user {} product {}: {}", userId, productId, e.getMessage(), e);
            throw new AppException("Failed to update cart quantity", e);
        }
    }

    @Override
    public boolean removeItem(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_ITEM)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error removing cart item for user {} product {}: {}", userId, productId, e.getMessage(), e);
            throw new AppException("Failed to remove item from cart", e);
        }
    }

    @Override
    public boolean clearCart(Long userId, Connection conn) {
        if (userId == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_CLEAR_CART)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Database error in clearCart with transaction for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to clear cart in transaction", e);
        }
    }

    @Override
    public boolean clearCart(Long userId) {
        if (userId == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection()) {
            return clearCart(userId, conn);
        } catch (SQLException e) {
            LOGGER.error("Database error in clearCart for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to clear cart", e);
        }
    }

    @Override
    public int countItemsByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ITEMS)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error counting cart items for user {}: {}", userId, e.getMessage(), e);
            throw new AppException("Failed to count cart items", e);
        }
        return 0;
    }

    private CartItem mapRowWithProduct(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setId(rs.getLong("id"));
        item.setUserId(rs.getLong("user_id"));
        item.setProductId(rs.getLong("product_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setCreatedAt(rs.getTimestamp("created_at"));

        Product p = new Product();
        p.setId(rs.getLong("product_id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("product_name"));
        p.setPrice(rs.getBigDecimal("product_price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCategory(rs.getString("category"));
        p.setAvgRating(rs.getBigDecimal("avg_rating"));
        p.setActive(rs.getBoolean("active"));
        p.setSellerName(rs.getString("seller_name"));

        item.setProduct(p);
        return item;
    }
}
