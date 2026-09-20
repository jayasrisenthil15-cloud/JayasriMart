package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.SellerDashboardDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.SellerService;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SellerService}.
 * Enforces strict multi-tenant seller ownership checks and inventory management rules.
 */
public class SellerServiceImpl implements SellerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SellerServiceImpl.class);
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;

    /**
     * Default constructor using {@link DaoFactory}.
     */
    public SellerServiceImpl() {
        this(DaoFactory.getProductDAO(), DaoFactory.getOrderDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param productDAO the product DAO
     * @param orderDAO the order DAO
     */
    public SellerServiceImpl(ProductDAO productDAO, OrderDAO orderDAO) {
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO cannot be null");
        }
        if (orderDAO == null) {
            throw new IllegalArgumentException("OrderDAO cannot be null");
        }
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public SellerDashboardDTO getDashboardData(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        int totalProducts = productDAO.countBySellerId(sellerId);
        int lowStockCount = productDAO.countLowStockBySellerId(sellerId, LOW_STOCK_THRESHOLD);
        int totalOrders = orderDAO.countBySellerId(sellerId);
        BigDecimal totalRevenue = orderDAO.calculateRevenueBySellerId(sellerId);

        List<Order> orders = orderDAO.findBySellerId(sellerId);
        List<Order> recentOrders = orders.stream().limit(5).collect(Collectors.toList());
        List<Product> lowStockProducts = productDAO.findLowStockBySellerId(sellerId, LOW_STOCK_THRESHOLD);

        return new SellerDashboardDTO(totalProducts, lowStockCount, totalOrders,
                totalRevenue, recentOrders, lowStockProducts);
    }

    @Override
    public List<Product> getProducts(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        return productDAO.findBySellerId(sellerId);
    }

    @Override
    public Product getProduct(Long sellerId, Long productId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!sellerId.equals(product.getSellerId())) {
            LOGGER.warn("Unauthorized seller product access: Seller {} tried accessing Product {} owned by Seller {}",
                    sellerId, productId, product.getSellerId());
            throw new UnauthorizedException("You are not authorized to view or edit this product.");
        }
        return product;
    }

    @Override
    public Product createProduct(Long sellerId, Product product) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (product == null) {
            throw new ValidationException("product", "Product payload cannot be null.");
        }

        validateProductFields(product);

        product.setSellerId(sellerId);
        if (product.getAvgRating() == null) {
            product.setAvgRating(BigDecimal.ZERO);
        }

        Product created = productDAO.create(product);
        LOGGER.info("Seller {} successfully created product ID {} ('{}')",
                sellerId, created.getId(), created.getName());
        return created;
    }

    @Override
    public Product updateProduct(Long sellerId, Product product) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (product == null || product.getId() == null) {
            throw new ValidationException("productId", "Product ID is required for updating.");
        }

        Product existing = productDAO.findById(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + product.getId()));

        if (!sellerId.equals(existing.getSellerId())) {
            LOGGER.warn("Unauthorized product update attempt: Seller {} tried modifying Product {} owned by Seller {}",
                    sellerId, product.getId(), existing.getSellerId());
            throw new UnauthorizedException("You can only modify products listed under your seller account.");
        }

        validateProductFields(product);

        existing.setName(product.getName().trim());
        existing.setDescription(product.getDescription() != null ? product.getDescription().trim() : "");
        existing.setPrice(product.getPrice());
        existing.setStockQty(product.getStockQty());
        existing.setCategory(product.getCategory().trim());
        existing.setImageUrl(product.getImageUrl() != null ? product.getImageUrl().trim() : "");
        existing.setActive(product.isActive());

        boolean updated = productDAO.update(existing);
        if (!updated) {
            throw new AppException("Failed to update product ID: " + product.getId());
        }
        LOGGER.info("Seller {} updated product ID {} ('{}')", sellerId, existing.getId(), existing.getName());
        return existing;
    }

    @Override
    public void deleteProduct(Long sellerId, Long productId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!sellerId.equals(existing.getSellerId())) {
            LOGGER.warn("Unauthorized product delete attempt: Seller {} tried deleting Product {} owned by Seller {}",
                    sellerId, productId, existing.getSellerId());
            throw new UnauthorizedException("You can only delete products listed under your seller account.");
        }

        boolean deleted = productDAO.delete(productId);
        if (!deleted) {
            throw new AppException("Failed to delete product ID: " + productId);
        }
        LOGGER.info("Seller {} deleted product ID {}", sellerId, productId);
    }

    @Override
    public List<Order> getOrders(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        return orderDAO.findBySellerId(sellerId);
    }

    @Override
    public void updateOrderStatus(Long sellerId, Long orderId, OrderStatus newStatus) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }
        if (newStatus == null) {
            throw new ValidationException("newStatus", "Target order status cannot be null.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        boolean containsSellerItems = order.getItems().stream()
                .anyMatch(item -> sellerId.equals(item.getSellerId()));

        if (!containsSellerItems) {
            LOGGER.warn("Unauthorized order status update: Seller {} tried updating Order {} with no items from this seller",
                    sellerId, orderId);
            throw new UnauthorizedException("You can only update orders that contain items from your store.");
        }

        OrderStatus currentStatus = order.getStatus();
        validateStatusTransition(currentStatus, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            cancelAndRestockOrder(order);
        } else {
            boolean updated = orderDAO.updateStatus(orderId, newStatus);
            if (!updated) {
                throw new AppException("Failed to update order status for order ID: " + orderId);
            }
        }

        LOGGER.info("Seller {} transitioned order ID {} from {} to {}",
                sellerId, orderId, currentStatus, newStatus);
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
                LOGGER.info("Order ID {} cancelled and restocked by seller", order.getId());
            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Failed to cancel and restock order ID {}: {}", order.getId(), e.getMessage(), e);
                throw new AppException("Failed to cancel order and restock inventory.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database connection error during order cancellation: {}", e.getMessage(), e);
            throw new AppException("Database error during order cancellation.", e);
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == null || target == null) {
            throw new ValidationException("status", "Order status cannot be null.");
        }
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new ValidationException("status",
                    "Cannot update status of an order that is already " + current);
        }
        if (!current.canTransitionTo(target)) {
            throw new ValidationException("status",
                    "Illegal status transition from " + current + " to " + target);
        }
    }

    private void validateProductFields(Product product) {
        String name = ValidationUtil.requireNonBlank(product.getName(), "name");
        if (name.length() < 2 || name.length() > 150) {
            throw new ValidationException("name", "Product name must be between 2 and 150 characters.");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("price", "Product price must be greater than zero.");
        }

        if (product.getStockQty() < 0) {
            throw new ValidationException("stockQty", "Stock quantity cannot be negative.");
        }

        String category = ValidationUtil.requireNonBlank(product.getCategory(), "category");
        if (category.length() < 2 || category.length() > 50) {
            throw new ValidationException("category", "Category must be between 2 and 50 characters.");
        }
    }
}
