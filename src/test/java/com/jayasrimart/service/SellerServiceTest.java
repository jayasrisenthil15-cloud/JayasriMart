package com.jayasrimart.service;

import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.SellerDashboardDTO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.impl.SellerServiceImpl;
import com.jayasrimart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private ProductDAO productDAO;

    @Mock
    private OrderDAO orderDAO;

    private SellerService sellerService;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_seller_service;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = SellerServiceTest.class.getClassLoader().getResourceAsStream("schema.sql");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
            String[] queries = sb.toString().split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query);
                }
            }
        }
    }

    @AfterAll
    static void tearDownAll() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void setUp() {
        sellerService = new SellerServiceImpl(productDAO, orderDAO);
    }

    @Test
    @DisplayName("getDashboardData aggregates metrics, revenue, and alerts correctly")
    void testGetDashboardData() {
        Long sellerId = 2L;
        when(productDAO.countBySellerId(sellerId)).thenReturn(15);
        when(productDAO.countLowStockBySellerId(sellerId, 5)).thenReturn(3);
        when(orderDAO.countBySellerId(sellerId)).thenReturn(8);
        when(orderDAO.calculateRevenueBySellerId(sellerId)).thenReturn(new BigDecimal("18500.00"));

        Order order = new Order();
        order.setId(101L);
        when(orderDAO.findBySellerId(sellerId)).thenReturn(List.of(order));

        Product lowStockProduct = new Product();
        lowStockProduct.setId(5L);
        lowStockProduct.setStockQty(2);
        when(productDAO.findLowStockBySellerId(sellerId, 5)).thenReturn(List.of(lowStockProduct));

        SellerDashboardDTO dto = sellerService.getDashboardData(sellerId);

        assertNotNull(dto);
        assertEquals(15, dto.getTotalProducts());
        assertEquals(3, dto.getLowStockCount());
        assertEquals(8, dto.getTotalOrders());
        assertEquals(0, new BigDecimal("18500.00").compareTo(dto.getTotalRevenue()));
        assertEquals(1, dto.getRecentOrders().size());
        assertEquals(1, dto.getLowStockProducts().size());
    }

    @Test
    @DisplayName("getProduct enforces seller ownership")
    void testGetProductOwnership() {
        Product p = new Product();
        p.setId(10L);
        p.setSellerId(2L);

        when(productDAO.findById(10L)).thenReturn(Optional.of(p));

        // Authorized owner
        Product found = sellerService.getProduct(2L, 10L);
        assertNotNull(found);
        assertEquals(10L, found.getId());

        // Unauthorized seller (ID 3)
        assertThrows(UnauthorizedException.class, () -> sellerService.getProduct(3L, 10L));
    }

    @Test
    @DisplayName("createProduct validates product fields and sets seller ID")
    void testCreateProductValidation() {
        Product invalid = new Product();
        invalid.setName("");
        assertThrows(ValidationException.class, () -> sellerService.createProduct(2L, invalid));

        Product valid = new Product();
        valid.setName("Cotton Polo T-Shirt");
        valid.setCategory("Fashion");
        valid.setPrice(new BigDecimal("799.00"));
        valid.setStockQty(25);
        valid.setActive(true);

        when(productDAO.create(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        Product created = sellerService.createProduct(2L, valid);
        assertNotNull(created);
        assertEquals(99L, created.getId());
        assertEquals(2L, created.getSellerId());
        verify(productDAO).create(valid);
    }

    @Test
    @DisplayName("updateProduct blocks unauthorized sellers and persists valid updates")
    void testUpdateProductOwnership() {
        Product existing = new Product();
        existing.setId(20L);
        existing.setSellerId(2L);
        existing.setName("Original Name");
        existing.setCategory("Fashion");
        existing.setPrice(new BigDecimal("500.00"));
        existing.setStockQty(10);

        when(productDAO.findById(20L)).thenReturn(Optional.of(existing));

        Product updated = new Product();
        updated.setId(20L);
        updated.setName("Updated Name");
        updated.setCategory("Fashion");
        updated.setPrice(new BigDecimal("600.00"));
        updated.setStockQty(15);
        updated.setActive(true);

        // Unauthorized seller 3
        assertThrows(UnauthorizedException.class, () -> sellerService.updateProduct(3L, updated));

        // Authorized seller 2
        when(productDAO.update(any(Product.class))).thenReturn(true);
        Product result = sellerService.updateProduct(2L, updated);
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(productDAO).update(existing);
    }

    @Test
    @DisplayName("deleteProduct blocks unauthorized sellers and deletes owned product")
    void testDeleteProductOwnership() {
        Product existing = new Product();
        existing.setId(30L);
        existing.setSellerId(2L);

        when(productDAO.findById(30L)).thenReturn(Optional.of(existing));

        // Unauthorized delete
        assertThrows(UnauthorizedException.class, () -> sellerService.deleteProduct(3L, 30L));
        verify(productDAO, never()).delete(anyLong());

        // Authorized delete
        when(productDAO.delete(30L)).thenReturn(true);
        sellerService.deleteProduct(2L, 30L);
        verify(productDAO).delete(30L);
    }

    @Test
    @DisplayName("updateOrderStatus validates seller items and transitions workflow")
    void testUpdateOrderStatusWorkflow() {
        Long sellerId = 2L;
        Order order = new Order();
        order.setId(50L);
        order.setStatus(OrderStatus.PENDING);

        OrderItem sellerItem = new OrderItem();
        sellerItem.setProductId(1L);
        sellerItem.setSellerId(sellerId);
        sellerItem.setQuantity(2);
        order.setItems(List.of(sellerItem));

        when(orderDAO.findById(50L)).thenReturn(Optional.of(order));
        when(orderDAO.updateStatus(50L, OrderStatus.CONFIRMED)).thenReturn(true);

        // Valid: PENDING -> CONFIRMED
        sellerService.updateOrderStatus(sellerId, 50L, OrderStatus.CONFIRMED);
        verify(orderDAO).updateStatus(50L, OrderStatus.CONFIRMED);

        // Illegal jump: PENDING -> DELIVERED (invalid transition)
        assertThrows(ValidationException.class, () -> sellerService.updateOrderStatus(sellerId, 50L, OrderStatus.DELIVERED));

        // Unauthorized seller (ID 99) not in order items
        assertThrows(UnauthorizedException.class, () -> sellerService.updateOrderStatus(99L, 50L, OrderStatus.CONFIRMED));
    }
}
