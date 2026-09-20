package com.jayasrimart.dao;

import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link Order} and {@link OrderItem} entities.
 */
public interface OrderDAO {

    /**
     * Creates an order record within an active database transaction.
     *
     * @param order the order entity
     * @param conn the active transaction Connection
     * @return the created order with populated generated ID and timestamp
     */
    Order create(Order order, Connection conn);

    /**
     * Batch inserts order line items within an active database transaction.
     *
     * @param items the list of order items
     * @param conn the active transaction Connection
     */
    void createOrderItems(List<OrderItem> items, Connection conn);

    /**
     * Finds an order by its ID, including all its line items and seller details.
     *
     * @param id the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(Long id);

    /**
     * Retrieves all orders placed by a specific buyer ordered by creation date descending.
     *
     * @param buyerId the buyer's user ID
     * @return list of orders with populated items
     */
    List<Order> findByBuyerId(Long buyerId);

    /**
     * Retrieves all incoming orders containing products from a specific seller.
     *
     * @param sellerId the seller's user ID
     * @return list of matching orders
     */
    List<Order> findBySellerId(Long sellerId);

    /**
     * Retrieves all orders in the entire system for administrative review.
     *
     * @return list of all orders
     */
    List<Order> findAll();

    /**
     * Updates an order status outside a transaction.
     *
     * @param orderId the order ID
     * @param status the new order status
     * @return true if updated, false otherwise
     */
    boolean updateStatus(Long orderId, OrderStatus status);

    /**
     * Updates an order status within an active transaction.
     *
     * @param orderId the order ID
     * @param status the new order status
     * @param conn the active transaction Connection
     * @return true if updated, false otherwise
     */
    boolean updateStatus(Long orderId, OrderStatus status, Connection conn);

    /**
     * Counts the total number of orders in the database.
     *
     * @return total order count
     */
    int countAll();

    /**
     * Counts the total number of orders placed by a specific buyer.
     *
     * @param buyerId the buyer's user ID
     * @return total count
     */
    int countByBuyerId(Long buyerId);

    /**
     * Counts the total number of incoming orders for a specific seller.
     *
     * @param sellerId the seller's user ID
     * @return total count
     */
    int countBySellerId(Long sellerId);

    /**
     * Calculates total revenue generated from items sold by a specific seller (excluding cancelled orders).
     *
     * @param sellerId the seller's user ID
     * @return total revenue amount
     */
    BigDecimal calculateRevenueBySellerId(Long sellerId);

    /**
     * Calculates platform-wide Gross Merchandise Value / total revenue (excluding cancelled orders).
     *
     * @return platform total revenue amount
     */
    BigDecimal calculateTotalRevenue();
}
