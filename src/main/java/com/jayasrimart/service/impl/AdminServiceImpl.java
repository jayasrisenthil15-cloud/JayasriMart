package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.UserDAO;
import com.jayasrimart.dto.AdminDashboardDTO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.service.AdminService;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AdminService}.
 * Provides platform-wide management, role assignment, product moderation, and order overrides.
 */
public class AdminServiceImpl implements AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    /**
     * Default constructor using {@link DaoFactory}.
     */
    public AdminServiceImpl() {
        this(DaoFactory.getUserDAO(), DaoFactory.getProductDAO(), DaoFactory.getOrderDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param userDAO the user DAO
     * @param productDAO the product DAO
     * @param orderDAO the order DAO
     */
    public AdminServiceImpl(UserDAO userDAO, ProductDAO productDAO, OrderDAO orderDAO) {
        if (userDAO == null) {
            throw new IllegalArgumentException("UserDAO cannot be null");
        }
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO cannot be null");
        }
        if (orderDAO == null) {
            throw new IllegalArgumentException("OrderDAO cannot be null");
        }
        this.userDAO = userDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public AdminDashboardDTO getDashboardData() {
        int totalUsers = userDAO.countAll();
        int totalBuyers = userDAO.countByRole(Role.BUYER);
        int totalSellers = userDAO.countByRole(Role.SELLER);
        int totalAdmins = userDAO.countByRole(Role.ADMIN);
        int totalProducts = productDAO.countAll();
        int totalOrders = orderDAO.countAll();
        BigDecimal totalRevenue = orderDAO.calculateTotalRevenue();

        List<User> allUsers = userDAO.findAll();
        List<User> recentUsers = allUsers.stream().limit(5).collect(Collectors.toList());

        List<Order> allOrders = orderDAO.findAll();
        List<Order> recentOrders = allOrders.stream().limit(5).collect(Collectors.toList());

        return new AdminDashboardDTO(totalUsers, totalBuyers, totalSellers, totalAdmins,
                totalProducts, totalOrders, totalRevenue, recentUsers, recentOrders);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public User updateUserRole(Long adminUserId, Long targetUserId, Role newRole) {
        if (adminUserId == null || adminUserId <= 0) {
            throw new ValidationException("adminUserId", "Invalid administrator user ID.");
        }
        if (targetUserId == null || targetUserId <= 0) {
            throw new ValidationException("targetUserId", "Invalid target user ID.");
        }
        if (newRole == null) {
            throw new ValidationException("newRole", "Target role cannot be null.");
        }

        if (adminUserId.equals(targetUserId)) {
            LOGGER.warn("Security violation: Admin {} attempted to modify their own role", adminUserId);
            throw new ValidationException("role", "Administrators cannot change their own role.");
        }

        User targetUser = userDAO.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + targetUserId));

        boolean updated = userDAO.updateRole(targetUserId, newRole);
        if (!updated) {
            throw new AppException("Failed to update user role for ID: " + targetUserId);
        }

        targetUser.setRole(newRole);
        LOGGER.info("Admin {} changed role of User ID {} ({}) to {}",
                adminUserId, targetUserId, targetUser.getEmail(), newRole);
        return targetUser;
    }

    @Override
    public List<Product> getAllProducts() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setLimit(1000);
        return productDAO.searchAndFilter(criteria);
    }

    @Override
    public Product toggleProductStatus(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        product.setActive(!product.isActive());
        boolean updated = productDAO.update(product);
        if (!updated) {
            throw new AppException("Failed to toggle product status for ID: " + productId);
        }

        LOGGER.info("Admin toggled product ID {} status to active={}", productId, product.isActive());
        return product;
    }

    @Override
    public void deleteProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        boolean deleted = productDAO.delete(productId);
        if (!deleted) {
            throw new AppException("Failed to delete product ID: " + productId);
        }

        LOGGER.info("Admin permanently deleted product ID {}", productId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }
        if (newStatus == null) {
            throw new ValidationException("newStatus", "Target status cannot be null.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("status",
                    "Cannot update status of an order that is already " + order.getStatus());
        }

        if (newStatus == OrderStatus.CANCELLED) {
            cancelAndRestockOrder(order);
        } else {
            boolean updated = orderDAO.updateStatus(orderId, newStatus);
            if (!updated) {
                throw new AppException("Failed to update status for order ID: " + orderId);
            }
        }

        LOGGER.info("Admin updated order ID {} status from {} to {}",
                orderId, order.getStatus(), newStatus);
    }

    private void cancelAndRestockOrder(Order order) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                orderDAO.updateStatus(order.getId(), OrderStatus.CANCELLED, conn);

                for (OrderItem item : order.getItems()) {
                    productDAO.restock(item.getProductId(), item.getQuantity(), conn);
                }

                conn.commit();
                LOGGER.info("Order ID {} cancelled and restocked by Admin override", order.getId());
            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Failed to cancel and restock order ID {}: {}", order.getId(), e.getMessage(), e);
                throw new AppException("Failed to cancel order and restock items.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database connection error in admin cancelAndRestockOrder: {}", e.getMessage(), e);
            throw new AppException("Database error during admin order cancellation.", e);
        }
    }
}
