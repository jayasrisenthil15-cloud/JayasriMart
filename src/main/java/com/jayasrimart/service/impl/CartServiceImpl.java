package com.jayasrimart.service.impl;

import com.jayasrimart.dao.CartDAO;
import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.CartResponseDTO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.CartItem;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.CartService;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link CartService}.
 * Enforces business logic and depends strictly on {@link CartDAO} and {@link ProductDAO} interfaces.
 */
public class CartServiceImpl implements CartService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public CartServiceImpl() {
        this(DaoFactory.getCartDAO(), DaoFactory.getProductDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param cartDAO the cart DAO interface
     * @param productDAO the product DAO interface
     */
    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        if (cartDAO == null) {
            throw new IllegalArgumentException("CartDAO dependency cannot be null");
        }
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO dependency cannot be null");
        }
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public CartResponseDTO getCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        List<CartItem> items = cartDAO.findByUserId(userId);
        return buildCartResponse(items);
    }

    @Override
    public CartResponseDTO addToCart(Long userId, Long productId, int quantity) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }
        ValidationUtil.validatePositiveQuantity(quantity, "quantity");

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.isActive()) {
            throw new ValidationException("product", "This product is currently inactive and cannot be purchased.");
        }

        Optional<CartItem> existingItem = cartDAO.findByUserAndProduct(userId, productId);
        int targetQty = existingItem.map(item -> item.getQuantity() + quantity).orElse(quantity);

        if (targetQty > product.getStockQty()) {
            throw new ValidationException("quantity",
                    "Cannot add " + quantity + " items. Total quantity (" + targetQty
                    + ") exceeds available stock of " + product.getStockQty() + ".");
        }

        cartDAO.addItem(userId, productId, quantity);
        LOGGER.info("User {} added {} units of product ID {} to cart", userId, quantity, productId);

        return getCart(userId);
    }

    @Override
    public CartResponseDTO updateCartItem(Long userId, Long productId, int quantity) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (quantity > product.getStockQty()) {
            throw new ValidationException("quantity",
                    "Requested quantity (" + quantity + ") exceeds available stock of " + product.getStockQty() + ".");
        }

        cartDAO.updateQuantity(userId, productId, quantity);
        LOGGER.info("User {} updated quantity to {} for product ID {}", userId, quantity, productId);

        return getCart(userId);
    }

    @Override
    public CartResponseDTO removeFromCart(Long userId, Long productId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        cartDAO.removeItem(userId, productId);
        LOGGER.info("User {} removed product ID {} from cart", userId, productId);

        return getCart(userId);
    }

    @Override
    public void clearCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        cartDAO.clearCart(userId);
        LOGGER.info("Cleared cart for user ID {}", userId);
    }

    @Override
    public int getCartCount(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return cartDAO.countItemsByUserId(userId);
    }

    private CartResponseDTO buildCartResponse(List<CartItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalCount = 0;

        for (CartItem item : items) {
            subtotal = subtotal.add(item.getSubtotal());
            totalCount += item.getQuantity();
        }

        BigDecimal deliveryCharge = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            if (subtotal.compareTo(CartResponseDTO.FREE_DELIVERY_THRESHOLD) >= 0) {
                deliveryCharge = BigDecimal.ZERO;
            } else {
                deliveryCharge = CartResponseDTO.STANDARD_DELIVERY_FEE;
            }
        }

        BigDecimal grandTotal = subtotal.add(deliveryCharge);
        return new CartResponseDTO(items, subtotal, deliveryCharge, grandTotal, totalCount);
    }
}
