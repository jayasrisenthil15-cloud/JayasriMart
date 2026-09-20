package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.OrderDaoImpl;
import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.ReviewDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderItem;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Review;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReviewDaoImplTest {

    private static ReviewDAO reviewDAO;
    private static OrderDAO orderDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;

    private static Long buyerId;
    private static Long sellerId;
    private static Long productId;
    private static Long orderId;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_review_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = ReviewDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        reviewDAO = new ReviewDaoImpl();
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
        buyer.setName("Aarav Sharma");
        buyer.setEmail("aarav@test.com");
        buyer.setPasswordHash("hash");
        buyer.setRole(Role.BUYER);
        buyerId = userDAO.create(buyer).getId();

        User seller = new User();
        seller.setName("Apex Trends");
        seller.setEmail("apex@test.com");
        seller.setPasswordHash("hash");
        seller.setRole(Role.SELLER);
        sellerId = userDAO.create(seller).getId();

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Oxford Shirt");
        product.setPrice(new BigDecimal("1299.00"));
        product.setStockQty(20);
        product.setCategory("Fashion");
        product.setActive(true);
        productId = productDAO.create(product).getId();

        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setTotalAmount(new BigDecimal("1299.00"));
        order.setDeliveryCharge(BigDecimal.ZERO);
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentMethod("UPI");
        order.setName("Aarav Sharma");
        order.setPhone("9876543210");
        order.setAddress("Address");
        order.setCity("Chennai");
        order.setPincode("600040");

        try (Connection conn = DBUtil.getConnection()) {
            Order createdOrder = orderDAO.create(order, conn);
            orderId = createdOrder.getId();

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(productId);
            item.setSellerId(sellerId);
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("1299.00"));
            orderDAO.createOrderItems(List.of(item), conn);
        }
    }

    @Test
    @DisplayName("Create review, find by product ID, and verify userName join")
    void testCreateAndFindReviews() throws Exception {
        Review r = new Review();
        r.setUserId(buyerId);
        r.setProductId(productId);
        r.setOrderId(orderId);
        r.setRating(5);
        r.setComment("Amazing shirt! Fits perfectly.");

        try (Connection conn = DBUtil.getConnection()) {
            Review created = reviewDAO.create(r, conn);
            assertNotNull(created.getId());
            assertNotNull(created.getCreatedAt());

            List<Review> reviews = reviewDAO.findByProductId(productId);
            assertEquals(1, reviews.size());
            assertEquals("Aarav Sharma", reviews.get(0).getUserName());
            assertEquals(5, reviews.get(0).getRating());
            assertEquals("Amazing shirt! Fits perfectly.", reviews.get(0).getComment());

            assertEquals(1, reviewDAO.countByProductId(productId));
        }
    }

    @Test
    @DisplayName("findByUserProductOrder detects previously submitted review")
    void testFindByUserProductOrder() throws Exception {
        Review r = new Review();
        r.setUserId(buyerId);
        r.setProductId(productId);
        r.setOrderId(orderId);
        r.setRating(4);
        r.setComment("Great quality.");

        try (Connection conn = DBUtil.getConnection()) {
            reviewDAO.create(r, conn);

            Optional<Review> existing = reviewDAO.findByUserProductOrder(buyerId, productId, orderId);
            assertTrue(existing.isPresent());
            assertEquals(4, existing.get().getRating());

            Optional<Review> notFound = reviewDAO.findByUserProductOrder(buyerId, productId, 999L);
            assertFalse(notFound.isPresent());
        }
    }

    @Test
    @DisplayName("calculateAverageRating computes average star rating correctly")
    void testCalculateAverageRating() throws Exception {
        try (Connection conn = DBUtil.getConnection()) {
            // First review: 5 stars
            Review r1 = new Review();
            r1.setUserId(buyerId);
            r1.setProductId(productId);
            r1.setOrderId(orderId);
            r1.setRating(5);
            r1.setComment("Five stars");
            reviewDAO.create(r1, conn);

            BigDecimal avg = reviewDAO.calculateAverageRating(productId, conn);
            assertEquals(0, new BigDecimal("5.00").compareTo(avg));
        }
    }
}
