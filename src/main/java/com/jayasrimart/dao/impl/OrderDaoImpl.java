package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
 * JDBC implementation of {@link OrderDAO}.
 */
public class OrderDaoImpl implements OrderDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDaoImpl.class);

    private static final String SQL_INSERT_ORDER =
            "INSERT INTO orders (buyer_id, total_amount, delivery_charge, status, payment_method, name, phone, address, city, pincode, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_ORDER_ITEM =
            "INSERT INTO order_items (order_id, product_id, seller_id, quantity, unit_price, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT o.id, o.buyer_id, o.total_amount, o.delivery_charge, o.status, o.payment_method, "
            + "o.name, o.phone, o.address, o.city, o.pincode, o.created_at, u.email AS buyer_email "
            + "FROM orders o "
            + "JOIN users u ON o.buyer_id = u.id "
            + "WHERE o.id = ?";

    private static final String SQL_FIND_ITEMS_BY_ORDER_ID =
            "SELECT oi.id, oi.order_id, oi.product_id, oi.seller_id, oi.quantity, oi.unit_price, oi.created_at, "
            + "p.name AS product_name, p.image_url AS product_image_url, u.name AS seller_name "
            + "FROM order_items oi "
            + "JOIN products p ON oi.product_id = p.id "
            + "JOIN users u ON oi.seller_id = u.id "
            + "WHERE oi.order_id = ? "
            + "ORDER BY oi.id ASC";

    private static final String SQL_FIND_BY_BUYER_ID =
            "SELECT o.id, o.buyer_id, o.total_amount, o.delivery_charge, o.status, o.payment_method, "
            + "o.name, o.phone, o.address, o.city, o.pincode, o.created_at, u.email AS buyer_email "
            + "FROM orders o "
            + "JOIN users u ON o.buyer_id = u.id "
            + "WHERE o.buyer_id = ? "
            + "ORDER BY o.created_at DESC";

    private static final String SQL_FIND_BY_SELLER_ID =
            "SELECT DISTINCT o.id, o.buyer_id, o.total_amount, o.delivery_charge, o.status, o.payment_method, "
            + "o.name, o.phone, o.address, o.city, o.pincode, o.created_at, u.email AS buyer_email "
            + "FROM orders o "
            + "JOIN users u ON o.buyer_id = u.id "
            + "JOIN order_items oi ON o.id = oi.order_id "
            + "WHERE oi.seller_id = ? "
            + "ORDER BY o.created_at DESC";

    private static final String SQL_FIND_ALL =
            "SELECT o.id, o.buyer_id, o.total_amount, o.delivery_charge, o.status, o.payment_method, "
            + "o.name, o.phone, o.address, o.city, o.pincode, o.created_at, u.email AS buyer_email "
            + "FROM orders o "
            + "JOIN users u ON o.buyer_id = u.id "
            + "ORDER BY o.created_at DESC";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE orders SET status = ? WHERE id = ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) AS total FROM orders";

    private static final String SQL_COUNT_BY_BUYER_ID =
            "SELECT COUNT(*) AS total FROM orders WHERE buyer_id = ?";

    private static final String SQL_COUNT_BY_SELLER_ID =
            "SELECT COUNT(DISTINCT o.id) AS total FROM orders o JOIN order_items oi ON o.id = oi.order_id WHERE oi.seller_id = ?";

    private static final String SQL_CALCULATE_REVENUE_BY_SELLER =
            "SELECT COALESCE(SUM(oi.quantity * oi.unit_price), 0.00) AS total_revenue "
            + "FROM order_items oi "
            + "JOIN orders o ON oi.order_id = o.id "
            + "WHERE oi.seller_id = ? AND o.status != 'CANCELLED'";

    private static final String SQL_CALCULATE_TOTAL_REVENUE =
            "SELECT COALESCE(SUM(total_amount), 0.00) AS gmv FROM orders WHERE status != 'CANCELLED'";

    @Override
    public Order create(Order order, Connection conn) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getBuyerId());
            ps.setBigDecimal(2, order.getTotalAmount());
            ps.setBigDecimal(3, order.getDeliveryCharge());
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getPaymentMethod());
            ps.setString(6, order.getName());
            ps.setString(7, order.getPhone());
            ps.setString(8, order.getAddress());
            ps.setString(9, order.getCity());
            ps.setString(10, order.getPincode());
            ps.setTimestamp(11, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AppException("Failed to create order: no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                    order.setCreatedAt(now);
                    return order;
                } else {
                    throw new AppException("Failed to retrieve generated ID for order.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error creating order for buyer {}: {}", order.getBuyerId(), e.getMessage(), e);
            throw new AppException("Failed to persist order", e);
        }
    }

    @Override
    public void createOrderItems(List<OrderItem> items, Connection conn) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ORDER_ITEM)) {
            for (OrderItem item : items) {
                ps.setLong(1, item.getOrderId());
                ps.setLong(2, item.getProductId());
                ps.setLong(3, item.getSellerId());
                ps.setInt(4, item.getQuantity());
                ps.setBigDecimal(5, item.getUnitPrice());
                ps.setTimestamp(6, now);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.error("Database error batch creating order items: {}", e.getMessage(), e);
            throw new AppException("Failed to batch persist order items", e);
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(order.getId(), conn));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findById for orderId {}: {}", id, e.getMessage(), e);
            throw new AppException("Failed to find order by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findByBuyerId(Long buyerId) {
        List<Order> orders = new ArrayList<>();
        if (buyerId == null) {
            return orders;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_BUYER_ID)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(order.getId(), conn));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findByBuyerId for buyerId {}: {}", buyerId, e.getMessage(), e);
            throw new AppException("Failed to retrieve buyer orders", e);
        }
        return orders;
    }

    @Override
    public List<Order> findBySellerId(Long sellerId) {
        List<Order> orders = new ArrayList<>();
        if (sellerId == null) {
            return orders;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SELLER_ID)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(findItemsByOrderId(order.getId(), conn));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findBySellerId for sellerId {}: {}", sellerId, e.getMessage(), e);
            throw new AppException("Failed to retrieve seller orders", e);
        }
        return orders;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order order = mapOrderRow(rs);
                order.setItems(findItemsByOrderId(order.getId(), conn));
                orders.add(order);
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findAll orders: {}", e.getMessage(), e);
            throw new AppException("Failed to retrieve all orders", e);
        }
        return orders;
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus status) {
        if (orderId == null || status == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection()) {
            return updateStatus(orderId, status, conn);
        } catch (SQLException e) {
            LOGGER.error("Database error updating order status: {}", e.getMessage(), e);
            throw new AppException("Failed to update order status", e);
        }
    }

    @Override
    public boolean updateStatus(Long orderId, OrderStatus status, Connection conn) {
        if (orderId == null || status == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            ps.setString(1, status.name());
            ps.setLong(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating order status inside transaction: {}", e.getMessage(), e);
            throw new AppException("Failed to update order status", e);
        }
    }

    @Override
    public int countAll() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.error("Database error counting all orders: {}", e.getMessage(), e);
            throw new AppException("Failed to count all orders", e);
        }
        return 0;
    }

    @Override
    public int countByBuyerId(Long buyerId) {
        if (buyerId == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_BUYER_ID)) {
            ps.setLong(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error counting buyer orders for {}: {}", buyerId, e.getMessage(), e);
            throw new AppException("Failed to count buyer orders", e);
        }
        return 0;
    }

    @Override
    public int countBySellerId(Long sellerId) {
        if (sellerId == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_SELLER_ID)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error counting seller orders for {}: {}", sellerId, e.getMessage(), e);
            throw new AppException("Failed to count seller orders", e);
        }
        return 0;
    }

    @Override
    public BigDecimal calculateRevenueBySellerId(Long sellerId) {
        if (sellerId == null) {
            return BigDecimal.ZERO;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CALCULATE_REVENUE_BY_SELLER)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total_revenue");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error calculating seller revenue for {}: {}", sellerId, e.getMessage(), e);
            throw new AppException("Failed to calculate seller revenue", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTotalRevenue() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CALCULATE_TOTAL_REVENUE);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal gmv = rs.getBigDecimal("gmv");
                return gmv != null ? gmv : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            LOGGER.error("Database error calculating total platform revenue: {}", e.getMessage(), e);
            throw new AppException("Failed to calculate total platform revenue", e);
        }
        return BigDecimal.ZERO;
    }

    private List<OrderItem> findItemsByOrderId(Long orderId, Connection conn) {
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_ITEMS_BY_ORDER_ID)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setSellerId(rs.getLong("seller_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));
                    item.setProductName(rs.getString("product_name"));
                    item.setProductImageUrl(rs.getString("product_image_url"));
                    item.setSellerName(rs.getString("seller_name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error finding items for orderId {}: {}", orderId, e.getMessage(), e);
            throw new AppException("Failed to retrieve order items", e);
        }
        return items;
    }

    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setBuyerId(rs.getLong("buyer_id"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setDeliveryCharge(rs.getBigDecimal("delivery_charge"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setPaymentMethod(rs.getString("payment_method"));
        order.setName(rs.getString("name"));
        order.setPhone(rs.getString("phone"));
        order.setAddress(rs.getString("address"));
        order.setCity(rs.getString("city"));
        order.setPincode(rs.getString("pincode"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            order.setBuyerEmail(rs.getString("buyer_email"));
        } catch (SQLException ignored) {
            // Column may not be in all projections
        }
        return order;
    }
}
