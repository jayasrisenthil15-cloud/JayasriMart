package com.jayasrimart.service;

import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.WishlistDAO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.impl.WishlistServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistDAO wishlistDAO;

    @Mock
    private ProductDAO productDAO;

    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        wishlistService = new WishlistServiceImpl(wishlistDAO, productDAO);
    }

    @Test
    @DisplayName("toggleWishlist adds product when not previously saved")
    void testToggleWishlistAdd() {
        Long userId = 4L;
        Long productId = 10L;

        when(productDAO.findById(productId)).thenReturn(Optional.of(new Product()));
        when(wishlistDAO.exists(userId, productId)).thenReturn(false);

        boolean inWishlist = wishlistService.toggleWishlist(userId, productId);

        assertTrue(inWishlist);
        verify(wishlistDAO).add(userId, productId);
        verify(wishlistDAO, never()).remove(anyLong(), anyLong());
    }

    @Test
    @DisplayName("toggleWishlist removes product when already saved")
    void testToggleWishlistRemove() {
        Long userId = 4L;
        Long productId = 10L;

        when(productDAO.findById(productId)).thenReturn(Optional.of(new Product()));
        when(wishlistDAO.exists(userId, productId)).thenReturn(true);

        boolean inWishlist = wishlistService.toggleWishlist(userId, productId);

        assertFalse(inWishlist);
        verify(wishlistDAO).remove(userId, productId);
        verify(wishlistDAO, never()).add(anyLong(), anyLong());
    }

    @Test
    @DisplayName("toggleWishlist validates input arguments")
    void testToggleWishlistValidation() {
        assertThrows(ValidationException.class, () -> wishlistService.toggleWishlist(null, 10L));
        assertThrows(ValidationException.class, () -> wishlistService.toggleWishlist(4L, null));
        assertThrows(ValidationException.class, () -> wishlistService.toggleWishlist(-1L, 10L));
    }

    @Test
    @DisplayName("toggleWishlist throws ResourceNotFoundException on non-existent product")
    void testToggleWishlistProductNotFound() {
        when(productDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> wishlistService.toggleWishlist(4L, 999L));
    }

    @Test
    @DisplayName("getRecommendations personalizes based on interacted categories")
    void testGetRecommendationsPersonalized() {
        Long userId = 4L;
        when(wishlistDAO.findProductIdsByUserId(userId)).thenReturn(List.of(1L));
        when(wishlistDAO.findInteractedCategories(userId)).thenReturn(List.of("Fashion"));

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Denim Jacket");
        p2.setCategory("Fashion");
        p2.setPrice(new BigDecimal("1999.00"));

        when(productDAO.findByCategory(eq("Fashion"), anyInt())).thenReturn(List.of(p2));

        List<Product> recs = wishlistService.getRecommendations(userId, 4);

        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        assertEquals(2L, recs.get(0).getId());
    }

    @Test
    @DisplayName("getRecommendations falls back to top-rated featured products when no history exists")
    void testGetRecommendationsFallback() {
        Product featured = new Product();
        featured.setId(5L);
        featured.setName("Smart Watch");

        when(productDAO.findFeatured(anyInt())).thenReturn(List.of(featured));

        List<Product> recs = wishlistService.getRecommendations(null, 4);

        assertNotNull(recs);
        assertEquals(1, recs.size());
        assertEquals(5L, recs.get(0).getId());
    }
}
