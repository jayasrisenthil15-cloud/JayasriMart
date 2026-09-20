package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.OrderDaoImpl;
import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderDaoImplTest {

    private static OrderDAO orderDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;

    private static Long buyerId;
    private static Long sellerId;
    private static Long productId;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_order_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = OrderDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        orderDAO = new OrderDaoImpl();
        productDAO = new ProductDaoImpl();
        userDAO = new UserDaoImpl();
    }

    @AfterAll
    static void tearDownAll() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void setUpTestData() throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reviews");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM cart_items");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
        }

        User buyer = new User();
        buyer.setName("Buyer Aarav");
        buyer.setEmail("aarav@test.com");
        buyer.setPasswordHash("hash");
        buyer.setRole(Role.BUYER);
        buyerId = userDAO.create(buyer).getId();

        User seller = new User();
        seller.setName("Seller Apex");
        seller.setEmail("apex@test.com");
        seller.setPasswordHash("hash");
        seller.setRole(Role.SELLER);
        sellerId = userDAO.create(seller).getId();

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Cotton Oxford Shirt");
        product.setPrice(new BigDecimal("1299.00"));
        product.setStockQty(20);
        product.setCategory("Fashion");
        product.setActive(true);
        productId = productDAO.create(product).getId();
    }

    @Test
    @DisplayName("Create order with line items and retrieve by ID, buyerId, and sellerId")
    void testOrderCreationAndRetrieval() throws Exception {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setTotalAmount(new BigDecimal("1299.00"));
        order.setDeliveryCharge(BigDecimal.ZERO);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentMethod("UPI");
        order.setName("Aarav Sharma");
        order.setPhone("9876543210");
        order.setAddress("Flat 402, Green Meadows");
        order.setCity("Chennai");
        order.setPincode("600040");

        try (Connection conn = DBUtil.getConnection()) {
            Order created = orderDAO.create(order, conn);
            assertNotNull(created.getId());

            OrderItem item = new OrderItem();
            item.setOrderId(created.getId());
            item.setProductId(productId);
            item.setSellerId(sellerId);
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("1299.00"));

            orderDAO.createOrderItems(List.of(item), conn);

            Optional<Order> fetched = orderDAO.findById(created.getId());
            assertTrue(fetched.isPresent());
            assertEquals("Aarav Sharma", fetched.get().getName());
            assertEquals(1, fetched.get().getItems().size());
            assertEquals("Cotton Oxford Shirt", fetched.get().getItems().get(0).getProductName());

            List<Order> buyerOrders = orderDAO.findByBuyerId(buyerId);
            assertEquals(1, buyerOrders.size());

            List<Order> sellerOrders = orderDAO.findBySellerId(sellerId);
            assertEquals(1, sellerOrders.size());

            assertEquals(1, orderDAO.countAll());
            assertEquals(1, orderDAO.countByBuyerId(buyerId));
            assertEquals(1, orderDAO.countBySellerId(sellerId));
        }
    }

    @Test
    @DisplayName("updateStatus updates status in database")
    void testUpdateStatus() throws Exception {
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setTotalAmount(new BigDecimal("500.00"));
        order.setDeliveryCharge(new BigDecimal("50.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod("COD");
        order.setName("Buyer Aarav");
        order.setPhone("9876543210");
        order.setAddress("Address");
        order.setCity("City");
        order.setPincode("600040");

        try (Connection conn = DBUtil.getConnection()) {
            Order created = orderDAO.create(order, conn);

            boolean updated = orderDAO.updateStatus(created.getId(), OrderStatus.CONFIRMED);
            assertTrue(updated);

            Optional<Order> fetched = orderDAO.findById(created.getId());
            assertTrue(fetched.isPresent());
            assertEquals(OrderStatus.CONFIRMED, fetched.get().getStatus());
        }
    }
}
