package com.jayasrimart.service;

import com.jayasrimart.dto.SellerDashboardDTO;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;

import java.util.List;

/**
 * Service interface managing business operations for the Seller Portal.
 */
public interface SellerService {

    /**
     * Retrieves aggregated metrics, recent orders, and inventory alerts for the seller dashboard.
     *
     * @param sellerId the seller's user ID
     * @return seller dashboard data transfer object
     */
    SellerDashboardDTO getDashboardData(Long sellerId);

    /**
     * Retrieves all products listed by a seller.
     *
     * @param sellerId the seller's user ID
     * @return list of products
     */
    List<Product> getProducts(Long sellerId);

    /**
     * Retrieves a single product belonging to a seller after verifying ownership.
     *
     * @param sellerId the seller's user ID
     * @param productId the product ID
     * @return the product
     */
    Product getProduct(Long sellerId, Long productId);

    /**
     * Creates and lists a new product under the seller's catalog.
     *
     * @param sellerId the seller's user ID
     * @param product the product entity
     * @return the newly created product
     */
    Product createProduct(Long sellerId, Product product);

    /**
     * Updates an existing product after verifying seller ownership.
     *
     * @param sellerId the seller's user ID
     * @param product the updated product entity
     * @return the updated product
     */
    Product updateProduct(Long sellerId, Product product);

    /**
     * Deletes a product after verifying seller ownership.
     *
     * @param sellerId the seller's user ID
     * @param productId the product ID
     */
    void deleteProduct(Long sellerId, Long productId);

    /**
     * Retrieves all incoming orders containing products sold by this seller.
     *
     * @param sellerId the seller's user ID
     * @return list of orders
     */
    List<Order> getOrders(Long sellerId);

    /**
     * Updates the fulfillment status of an order after verifying seller ownership and status workflow rules.
     *
     * @param sellerId the seller's user ID
     * @param orderId the order ID
     * @param newStatus the target order status
     */
    void updateOrderStatus(Long sellerId, Long orderId, OrderStatus newStatus);
}
