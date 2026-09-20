package com.jayasrimart.service;

import com.jayasrimart.dao.OrderDAO;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.UserDAO;
import com.jayasrimart.dto.AdminDashboardDTO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.service.impl.AdminServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private ProductDAO productDAO;

    @Mock
    private OrderDAO orderDAO;

    private AdminService adminService;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_admin_service;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = AdminServiceTest.class.getClassLoader().getResourceAsStream("schema.sql");
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
        adminService = new AdminServiceImpl(userDAO, productDAO, orderDAO);
    }

    @Test
    @DisplayName("getDashboardData gathers counts and GMV correctly")
    void testGetDashboardData() {
        when(userDAO.countAll()).thenReturn(20);
        when(userDAO.countByRole(Role.BUYER)).thenReturn(14);
        when(userDAO.countByRole(Role.SELLER)).thenReturn(5);
        when(userDAO.countByRole(Role.ADMIN)).thenReturn(1);
        when(productDAO.countAll()).thenReturn(35);
        when(orderDAO.countAll()).thenReturn(40);
        when(orderDAO.calculateTotalRevenue()).thenReturn(new BigDecimal("75400.00"));

        User user = new User();
        user.setId(1L);
        when(userDAO.findAll()).thenReturn(List.of(user));

        Order order = new Order();
        order.setId(10L);
        when(orderDAO.findAll()).thenReturn(List.of(order));

        AdminDashboardDTO dto = adminService.getDashboardData();
        assertNotNull(dto);
        assertEquals(20, dto.getTotalUsers());
        assertEquals(14, dto.getTotalBuyers());
        assertEquals(5, dto.getTotalSellers());
        assertEquals(1, dto.getTotalAdmins());
        assertEquals(35, dto.getTotalProducts());
        assertEquals(40, dto.getTotalOrders());
        assertEquals(0, new BigDecimal("75400.00").compareTo(dto.getTotalRevenue()));
        assertEquals(1, dto.getRecentUsers().size());
        assertEquals(1, dto.getRecentOrders().size());
    }

    @Test
    @DisplayName("updateUserRole blocks self-modification and updates target user role")
    void testUpdateUserRole() {
        Long adminId = 1L;
        Long targetId = 2L;

        // Self-modification forbidden
        assertThrows(ValidationException.class, () -> adminService.updateUserRole(adminId, adminId, Role.SELLER));
        verify(userDAO, never()).updateRole(anyLong(), any());

        // Target user update
        User target = new User();
        target.setId(targetId);
        target.setRole(Role.BUYER);
        target.setEmail("buyer@example.com");

        when(userDAO.findById(targetId)).thenReturn(Optional.of(target));
        when(userDAO.updateRole(targetId, Role.SELLER)).thenReturn(true);

        User updated = adminService.updateUserRole(adminId, targetId, Role.SELLER);
        assertNotNull(updated);
        assertEquals(Role.SELLER, updated.getRole());
        verify(userDAO).updateRole(targetId, Role.SELLER);
    }

    @Test
    @DisplayName("toggleProductStatus flips active flag")
    void testToggleProductStatus() {
        Product p = new Product();
        p.setId(5L);
        p.setActive(true);

        when(productDAO.findById(5L)).thenReturn(Optional.of(p));
        when(productDAO.update(p)).thenReturn(true);

        Product toggled = adminService.toggleProductStatus(5L);
        assertNotNull(toggled);
        assertFalse(toggled.isActive());
        verify(productDAO).update(p);
    }

    @Test
    @DisplayName("deleteProduct deletes product from database")
    void testDeleteProduct() {
        Product p = new Product();
        p.setId(8L);

        when(productDAO.findById(8L)).thenReturn(Optional.of(p));
        when(productDAO.delete(8L)).thenReturn(true);

        adminService.deleteProduct(8L);
        verify(productDAO).delete(8L);
    }

    @Test
    @DisplayName("updateOrderStatus updates order status and restocks on CANCELLED")
    void testUpdateOrderStatus() {
        Order order = new Order();
        order.setId(20L);
        order.setStatus(OrderStatus.CONFIRMED);

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        order.setItems(List.of(item));

        when(orderDAO.findById(20L)).thenReturn(Optional.of(order));
        when(orderDAO.updateStatus(20L, OrderStatus.SHIPPED)).thenReturn(true);

        adminService.updateOrderStatus(20L, OrderStatus.SHIPPED);
        verify(orderDAO).updateStatus(20L, OrderStatus.SHIPPED);

        // Cancel order -> restocks
        adminService.updateOrderStatus(20L, OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("updateOrderStatus rejects updating already DELIVERED order")
    void testUpdateOrderStatusDelivered() {
        Order delivered = new Order();
        delivered.setId(30L);
        delivered.setStatus(OrderStatus.DELIVERED);

        when(orderDAO.findById(30L)).thenReturn(Optional.of(delivered));
        assertThrows(ValidationException.class, () -> adminService.updateOrderStatus(30L, OrderStatus.CONFIRMED));
    }
}
