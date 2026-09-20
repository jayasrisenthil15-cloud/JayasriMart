package com.jayasrimart.service.impl;

import com.jayasrimart.dao.CartDAO;
import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.CartResponseDTO;
import com.jayasrimart.dto.CheckoutRequestDTO;
import com.jayasrimart.dto.OrderResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.CartItem;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Role;
import com.jayasrimart.service.OrderService;
import com.jayasrimart.service.payment.PaymentRequest;
import com.jayasrimart.service.payment.PaymentResult;
import com.jayasrimart.service.payment.PaymentStrategy;
import com.jayasrimart.service.payment.PaymentStrategyFactory;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link OrderService}.
 * Enforces business logic and depends strictly on {@link OrderDAO}, {@link CartDAO}, and {@link ProductDAO} interfaces.
 */
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public OrderServiceImpl() {
        this(DaoFactory.getOrderDAO(), DaoFactory.getCartDAO(), DaoFactory.getProductDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param orderDAO the order DAO interface
     * @param cartDAO the cart DAO interface
     * @param productDAO the product DAO interface
     */
    public OrderServiceImpl(OrderDAO orderDAO, CartDAO cartDAO, ProductDAO productDAO) {
        if (orderDAO == null) {
            throw new IllegalArgumentException("OrderDAO dependency cannot be null");
        }
        if (cartDAO == null) {
            throw new IllegalArgumentException("CartDAO dependency cannot be null");
        }
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO dependency cannot be null");
        }
        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public OrderResponseDTO placeOrder(Long buyerId, CheckoutRequestDTO checkoutRequest) {
        // 1. Server-side validation at top of service method
        if (buyerId == null || buyerId <= 0) {
            throw new ValidationException("buyerId", "Invalid buyer user ID.");
        }
        if (checkoutRequest == null) {
            throw new ValidationException("checkout", "Checkout request payload cannot be null.");
        }

        String name = ValidationUtil.requireNonBlank(checkoutRequest.getName(), "name");
        String phone = ValidationUtil.validatePhone(checkoutRequest.getPhone());
        String address = ValidationUtil.requireNonBlank(checkoutRequest.getAddress(), "address");
        String city = ValidationUtil.requireNonBlank(checkoutRequest.getCity(), "city");
        String pincode = ValidationUtil.validatePincode(checkoutRequest.getPincode());

        if (checkoutRequest.getPaymentMethod() == null) {
            throw new ValidationException("paymentMethod", "Please select a payment method.");
        }

        // 2. Fetch cart and verify it contains items
        List<CartItem> cartItems = cartDAO.findByUserId(buyerId);
        if (cartItems.isEmpty()) {
            throw new ValidationException("cart", "Your shopping cart is empty. Please add items before placing an order.");
        }

        // 3. Pre-verify stock availability and active status for all cart items
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = productDAO.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + item.getProductId()));

            if (!product.isActive()) {
                throw new ValidationException("product", "Product '" + product.getName() + "' is no longer available for purchase.");
            }

            if (item.getQuantity() > product.getStockQty()) {
                throw new ValidationException("stock", "Insufficient stock for '" + product.getName() + "'. Available: "
                        + product.getStockQty() + ", in cart: " + item.getQuantity() + ".");
            }

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 4. Calculate delivery fee rule: flat Rs. 50, free if subtotal >= Rs. 999
        BigDecimal deliveryCharge = subtotal.compareTo(CartResponseDTO.FREE_DELIVERY_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : CartResponseDTO.STANDARD_DELIVERY_FEE;
        BigDecimal grandTotal = subtotal.add(deliveryCharge);

        // 5. Execute Mock Payment Strategy
        PaymentStrategy strategy = PaymentStrategyFactory.getStrategy(checkoutRequest.getPaymentMethod());
        PaymentRequest paymentRequest = new PaymentRequest(
                buyerId,
                grandTotal,
                checkoutRequest.getPaymentMethod(),
                checkoutRequest.getUpiId(),
                checkoutRequest.getCardNumber(),
                checkoutRequest.getCardExpiry(),
                checkoutRequest.getCardCvv()
        );

        PaymentResult paymentResult = strategy.processPayment(paymentRequest);
        if (!paymentResult.isSuccessful()) {
            LOGGER.warn("Payment failed for buyer {}: {}", buyerId, paymentResult.getMessage());
            throw new ValidationException("payment", paymentResult.getMessage());
        }

        LOGGER.info("Payment approved via strategy {}. Txn: {}", checkoutRequest.getPaymentMethod(), paymentResult.getTransactionId());

        // 6. Execute Atomic Transaction: Order Creation + Stock Reduction + Cart Clearance
        Order newOrder = new Order();
        newOrder.setBuyerId(buyerId);
        newOrder.setTotalAmount(grandTotal);
        newOrder.setDeliveryCharge(deliveryCharge);
        newOrder.setStatus(OrderStatus.CONFIRMED);
        newOrder.setPaymentMethod(checkoutRequest.getPaymentMethod().name());
        newOrder.setName(name);
        newOrder.setPhone(phone);
        newOrder.setAddress(address);
        newOrder.setCity(city);
        newOrder.setPincode(pincode);

        List<OrderItem> orderItems = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert Order record
                Order createdOrder = orderDAO.create(newOrder, conn);

                // Deduct stock and build OrderItem entities
                for (CartItem item : cartItems) {
                    boolean stockUpdated = productDAO.updateStock(item.getProductId(), item.getQuantity(), conn);
                    if (!stockUpdated) {
                        throw new ValidationException("stock", "Failed to reserve stock for '"
                                + item.getProduct().getName() + "'. Stock may have been depleted by another purchase.");
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(createdOrder.getId());
                    orderItem.setProductId(item.getProductId());
                    orderItem.setSellerId(item.getProduct().getSellerId());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(item.getProduct().getPrice());
                    orderItem.setProductName(item.getProduct().getName());
                    orderItem.setProductImageUrl(item.getProduct().getImageUrl());
                    orderItem.setSellerName(item.getProduct().getSellerName());
                    orderItems.add(orderItem);
                }

                // Insert Order Items batch
                orderDAO.createOrderItems(orderItems, conn);

                // Clear buyer's cart
                cartDAO.clearCart(buyerId, conn);

                conn.commit();
                LOGGER.info("Successfully committed order ID {} for buyer ID {}", createdOrder.getId(), buyerId);

                return OrderResponseDTO.builder()
                        .id(createdOrder.getId())
                        .buyerId(buyerId)
                        .totalAmount(grandTotal)
                        .deliveryCharge(deliveryCharge)
                        .status(createdOrder.getStatus())
                        .paymentMethod(createdOrder.getPaymentMethod())
                        .name(name)
                        .phone(phone)
                        .address(address)
                        .city(city)
                        .pincode(pincode)
                        .createdAt(createdOrder.getCreatedAt())
                        .items(orderItems)
                        .build();

            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Transaction rolled back during order creation for buyer {}: {}", buyerId, e.getMessage(), e);
                if (e instanceof AppException appException) {
                    throw appException;
                }
                throw new AppException("Failed to complete order checkout. Your cart remains intact.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database connection error during order checkout: {}", e.getMessage(), e);
            throw new AppException("Database error during order placement.", e);
        }
    }

    @Override
    public Order getOrderDetails(Long userId, Long orderId, Role role) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Enforce RBAC access checks
        if (Role.BUYER.equals(role)) {
            if (!userId.equals(order.getBuyerId())) {
                LOGGER.warn("Unauthorized order access: Buyer {} attempted accessing Order {} belonging to Buyer {}",
                        userId, orderId, order.getBuyerId());
                throw new UnauthorizedException("You are not authorized to view this order.");
            }
        } else if (Role.SELLER.equals(role)) {
            boolean hasSellerItem = order.getItems().stream()
                    .anyMatch(item -> userId.equals(item.getSellerId()));
            if (!hasSellerItem) {
                LOGGER.warn("Unauthorized order access: Seller {} attempted accessing Order {} containing no items for this seller",
                        userId, orderId);
                throw new UnauthorizedException("You are not authorized to view this order.");
            }
        }

        return order;
    }

    @Override
    public List<Order> getBuyerOrders(Long buyerId) {
        if (buyerId == null || buyerId <= 0) {
            throw new ValidationException("buyerId", "Invalid buyer user ID.");
        }
        return orderDAO.findByBuyerId(buyerId);
    }

    @Override
    public void cancelOrder(Long buyerId, Long orderId) {
        if (buyerId == null || buyerId <= 0) {
            throw new ValidationException("buyerId", "Invalid buyer user ID.");
        }
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!buyerId.equals(order.getBuyerId())) {
            throw new UnauthorizedException("You can only cancel your own orders.");
        }

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new ValidationException("status",
                    "Cannot cancel order. Cancellation is only allowed for orders in PENDING or CONFIRMED state. Current state: "
                    + order.getStatus());
        }

        // Execute transaction: update status + restock items
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                orderDAO.updateStatus(orderId, OrderStatus.CANCELLED, conn);

                // Restock inventory for each order line item
                for (OrderItem item : order.getItems()) {
                    productDAO.restock(item.getProductId(), item.getQuantity(), conn);
                }

                conn.commit();
                LOGGER.info("Order ID {} cancelled and items restocked by buyer ID {}", orderId, buyerId);
            } catch (Exception e) {
                conn.rollback();
                LOGGER.error("Failed to cancel order ID {}: {}", orderId, e.getMessage(), e);
                throw new AppException("Failed to cancel order.", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Database connection error cancelling order {}: {}", orderId, e.getMessage(), e);
            throw new AppException("Database error during order cancellation.", e);
        }
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("orderId", "Invalid order ID.");
        }
        if (newStatus == null) {
            throw new ValidationException("status", "Target order status cannot be null.");
        }

        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new ValidationException("status", "Illegal status transition from '"
                    + order.getStatus() + "' to '" + newStatus + "'.");
        }

        orderDAO.updateStatus(orderId, newStatus);
        LOGGER.info("Updated order ID {} status from {} to {}", orderId, order.getStatus(), newStatus);
    }
}
