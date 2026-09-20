package com.jayasrimart.service;

import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.ReviewDAO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private ReviewDAO reviewDAO;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productDAO, reviewDAO);
    }

    @Test
    @DisplayName("getProductDetails returns product when found and throws exception when not found")
    void testGetProductDetails() {
        Product mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("Sample Book");

        when(productDAO.findById(100L)).thenReturn(Optional.of(mockProduct));
        when(productDAO.findById(999L)).thenReturn(Optional.empty());

        Product p = productService.getProductDetails(100L);
        assertNotNull(p);
        assertEquals("Sample Book", p.getName());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductDetails(999L));
        assertThrows(ValidationException.class, () -> productService.getProductDetails(null));
        assertThrows(ValidationException.class, () -> productService.getProductDetails(0L));
    }

    @Test
    @DisplayName("searchProducts rejects search criteria when minPrice > maxPrice")
    void testSearchProductsInvalidPriceRange() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .minPrice(new BigDecimal("500.00"))
                .maxPrice(new BigDecimal("100.00"))
                .build();

        assertThrows(ValidationException.class, () -> productService.searchProducts(criteria));
        verify(productDAO, never()).searchAndFilter(any());
    }

    @Test
    @DisplayName("createProduct validates product fields and persists")
    void testCreateProduct() {
        Product newProduct = new Product();
        newProduct.setName("Linen Shirt");
        newProduct.setCategory("Fashion");
        newProduct.setPrice(new BigDecimal("1299.00"));
        newProduct.setStockQty(15);
        newProduct.setDescription("Pure linen comfortable summer shirt");

        when(productDAO.create(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(50L);
            return p;
        });

        Product created = productService.createProduct(2L, newProduct);
        assertNotNull(created);
        assertEquals(50L, created.getId());
        assertEquals(2L, created.getSellerId());
        verify(productDAO).create(any(Product.class));
    }

    @Test
    @DisplayName("createProduct throws ValidationException on invalid input")
    void testCreateProductValidation() {
        Product invalidPrice = new Product();
        invalidPrice.setName("Valid Name");
        invalidPrice.setCategory("Fashion");
        invalidPrice.setPrice(BigDecimal.ZERO); // price must be > 0
        invalidPrice.setStockQty(5);

        assertThrows(ValidationException.class, () -> productService.createProduct(2L, invalidPrice));

        Product negativeStock = new Product();
        negativeStock.setName("Valid Name");
        negativeStock.setCategory("Fashion");
        negativeStock.setPrice(new BigDecimal("100.00"));
        negativeStock.setStockQty(-1);

        assertThrows(ValidationException.class, () -> productService.createProduct(2L, negativeStock));
    }

    @Test
    @DisplayName("updateProduct enforces seller ownership check")
    void testUpdateProductOwnership() {
        Product existing = new Product();
        existing.setId(10L);
        existing.setSellerId(2L); // Owned by seller 2
        existing.setName("Original Name");
        existing.setCategory("Fashion");
        existing.setPrice(new BigDecimal("500.00"));

        when(productDAO.findById(10L)).thenReturn(Optional.of(existing));

        Product updatePayload = new Product();
        updatePayload.setId(10L);
        updatePayload.setName("Updated Name");
        updatePayload.setCategory("Fashion");
        updatePayload.setPrice(new BigDecimal("600.00"));
        updatePayload.setStockQty(10);

        // Authorized: seller 2 updates product 10
        Product updated = productService.updateProduct(2L, updatePayload);
        assertNotNull(updated);
        assertEquals("Updated Name", updated.getName());
        verify(productDAO).update(any(Product.class));

        // Unauthorized: seller 3 attempts to update product 10 owned by seller 2
        assertThrows(UnauthorizedException.class, () -> productService.updateProduct(3L, updatePayload));
    }

    @Test
    @DisplayName("deleteProduct enforces seller ownership check")
    void testDeleteProductOwnership() {
        Product existing = new Product();
        existing.setId(20L);
        existing.setSellerId(2L); // Owned by seller 2

        when(productDAO.findById(20L)).thenReturn(Optional.of(existing));

        // Unauthorized: seller 3 tries to delete seller 2's product
        assertThrows(UnauthorizedException.class, () -> productService.deleteProduct(3L, 20L));
        verify(productDAO, never()).delete(20L);

        // Authorized: seller 2 deletes seller 2's product
        productService.deleteProduct(2L, 20L);
        verify(productDAO).delete(20L);
    }
}
