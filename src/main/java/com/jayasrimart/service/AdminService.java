package com.jayasrimart.service;

import com.jayasrimart.dto.AdminDashboardDTO;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;

import java.util.List;

/**
 * Service interface defining administrative platform management and moderation operations.
 */
public interface AdminService {

    /**
     * Retrieves aggregated system health metrics, revenue summaries, and recent activity logs.
     *
     * @return platform administration dashboard DTO
     */
    AdminDashboardDTO getDashboardData();

    /**
     * Retrieves all registered user accounts.
     *
     * @return list of all users
     */
    List<User> getAllUsers();

    /**
     * Updates the access role of a target user (e.g. promoting Buyer to Seller or Admin).
     * Enforces the security constraint that an administrator cannot demote or modify their own role.
     *
     * @param adminUserId the ID of the executing administrator
     * @param targetUserId the ID of the user whose role is being changed
     * @param newRole the target role
     * @return updated user entity
     */
    User updateUserRole(Long adminUserId, Long targetUserId, Role newRole);

    /**
     * Retrieves all products across all sellers in the system.
     *
     * @return list of all products
     */
    List<Product> getAllProducts();

    /**
     * Toggles the active/visibility status of a product (moderation toggle).
     *
     * @param productId the product ID
     * @return updated product entity
     */
    Product toggleProductStatus(Long productId);

    /**
     * Permanently deletes a product from the platform catalog.
     *
     * @param productId the product ID
     */
    void deleteProduct(Long productId);

    /**
     * Retrieves all customer orders placed across the entire platform.
     *
     * @return list of all orders
     */
    List<Order> getAllOrders();

    /**
     * Updates the status of any platform order (administrative override), managing inventory restock on cancellation.
     *
     * @param orderId the order ID
     * @param newStatus the new status
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus);
}
