package com.jayasrimart.service;

import com.jayasrimart.dto.CheckoutRequestDTO;
import com.jayasrimart.dto.OrderResponseDTO;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Role;

import java.util.List;

/**
 * Service interface managing order placement, transactional checkout, lifecycle status transitions,
 * and order history queries.
 */
public interface OrderService {

    /**
     * Places a customer order in ONE atomic ACID transaction: verifies stock, processes mock payment via Strategy,
     * creates order & line items, reduces product stock, and clears the buyer's cart.
     *
     * @param buyerId authenticated buyer user ID
     * @param checkoutRequest delivery address and mock payment information
     * @return populated {@link OrderResponseDTO}
     */
    OrderResponseDTO placeOrder(Long buyerId, CheckoutRequestDTO checkoutRequest);

    /**
     * Retrieves full order details ensuring access authorization based on user role.
     *
     * @param userId requesting user ID
     * @param orderId the order ID
     * @param role requesting user's role
     * @return the populated Order entity
     */
    Order getOrderDetails(Long userId, Long orderId, Role role);

    /**
     * Retrieves all orders placed by a specific buyer.
     *
     * @param buyerId buyer user ID
     * @return list of orders
     */
    List<Order> getBuyerOrders(Long buyerId);

    /**
     * Cancels an order, enforcing status rules (permitted only from PENDING or CONFIRMED),
     * and restocks product inventory within a transaction.
     *
     * @param buyerId authenticated buyer user ID
     * @param orderId the order ID
     */
    void cancelOrder(Long buyerId, Long orderId);

    /**
     * Updates an order status enforcing strict lifecycle transitions.
     *
     * @param orderId the order ID
     * @param newStatus the target status
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus);
}
