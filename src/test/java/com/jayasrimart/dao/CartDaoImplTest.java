package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.CartDaoImpl;
import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.model.CartItem;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartDaoImplTest {

    private static CartDAO cartDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;

    private static Long buyerId;
    private static Long sellerId;
    private static Long productId;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_cart_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = CartDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        cartDAO = new CartDaoImpl();
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
        product.setName("Cotton Shirt");
        product.setPrice(new BigDecimal("1299.00"));
        product.setStockQty(20);
        product.setCategory("Fashion");
        product.setActive(true);
        productId = productDAO.create(product).getId();
    }

    @Test
    @DisplayName("addItem, updateQuantity, findByUserId, and removeItem work correctly")
    void testCartLifecycle() {
        // 1. Add item
        CartItem added = cartDAO.addItem(buyerId, productId, 2);
        assertNotNull(added.getId());
        assertEquals(2, added.getQuantity());

        // 2. Query items
        List<CartItem> items = cartDAO.findByUserId(buyerId);
        assertEquals(1, items.size());
        assertEquals("Cotton Shirt", items.get(0).getProduct().getName());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(0, new BigDecimal("2598.00").compareTo(items.get(0).getSubtotal()));

        // 3. Increment via addItem
        cartDAO.addItem(buyerId, productId, 1);
        assertEquals(3, cartDAO.countItemsByUserId(buyerId));

        // 4. Update exact quantity
        cartDAO.updateQuantity(buyerId, productId, 5);
        assertEquals(5, cartDAO.countItemsByUserId(buyerId));

        // 5. Remove item
        boolean removed = cartDAO.removeItem(buyerId, productId);
        assertTrue(removed);
        assertEquals(0, cartDAO.countItemsByUserId(buyerId));
    }

    @Test
    @DisplayName("clearCart removes all cart items for user")
    void testClearCart() {
        cartDAO.addItem(buyerId, productId, 2);
        assertEquals(2, cartDAO.countItemsByUserId(buyerId));

        cartDAO.clearCart(buyerId);
        assertEquals(0, cartDAO.countItemsByUserId(buyerId));
    }
}
