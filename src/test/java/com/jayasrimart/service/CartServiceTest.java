package com.jayasrimart.service;

import com.jayasrimart.dao.CartDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.CartResponseDTO;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.CartItem;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartDAO cartDAO;

    @Mock
    private ProductDAO productDAO;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartDAO, productDAO);
    }

    @Test
    @DisplayName("getCart calculates free delivery when subtotal >= 999.00")
    void testGetCartFreeDelivery() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Leather Jacket");
        p.setPrice(new BigDecimal("1299.00"));

        CartItem item = new CartItem(10L, 4L, 1L, 1, null);
        item.setProduct(p);

        when(cartDAO.findByUserId(4L)).thenReturn(List.of(item));

        CartResponseDTO response = cartService.getCart(4L);
        assertNotNull(response);
        assertEquals(0, new BigDecimal("1299.00").compareTo(response.getSubtotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDeliveryCharge()));
        assertEquals(0, new BigDecimal("1299.00").compareTo(response.getGrandTotal()));
        assertTrue(response.isFreeDelivery());
        assertEquals(1, response.getTotalItems());
    }

    @Test
    @DisplayName("getCart adds flat 50 delivery fee when subtotal < 999.00")
    void testGetCartStandardDeliveryFee() {
        Product p = new Product();
        p.setId(2L);
        p.setName("T-Shirt");
        p.setPrice(new BigDecimal("499.00"));

        CartItem item = new CartItem(11L, 4L, 2L, 1, null);
        item.setProduct(p);

        when(cartDAO.findByUserId(4L)).thenReturn(List.of(item));

        CartResponseDTO response = cartService.getCart(4L);
        assertNotNull(response);
        assertEquals(0, new BigDecimal("499.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("50.00").compareTo(response.getDeliveryCharge()));
        assertEquals(0, new BigDecimal("549.00").compareTo(response.getGrandTotal()));
        assertFalse(response.isFreeDelivery());
        assertEquals(0, new BigDecimal("500.00").compareTo(response.getAmountNeededForFreeDelivery()));
    }

    @Test
    @DisplayName("addToCart validates stock and prevents adding more than available")
    void testAddToCartStockCheck() {
        Product p = new Product();
        p.setId(5L);
        p.setName("Running Shoes");
        p.setPrice(new BigDecimal("1500.00"));
        p.setStockQty(3); // Only 3 in stock
        p.setActive(true);

        when(productDAO.findById(5L)).thenReturn(Optional.of(p));
        when(cartDAO.findByUserAndProduct(4L, 5L)).thenReturn(Optional.empty());

        // Attempt to add 4 items -> should fail
        assertThrows(ValidationException.class, () -> cartService.addToCart(4L, 5L, 4));
        verify(cartDAO, never()).addItem(anyLong(), anyLong(), anyInt());

        // Attempt to add 2 items -> should succeed
        cartService.addToCart(4L, 5L, 2);
        verify(cartDAO).addItem(4L, 5L, 2);
    }

    @Test
    @DisplayName("updateCartItem with quantity <= 0 removes item")
    void testUpdateCartItemZeroRemoves() {
        cartService.updateCartItem(4L, 5L, 0);
        verify(cartDAO).removeItem(4L, 5L);
    }
}
