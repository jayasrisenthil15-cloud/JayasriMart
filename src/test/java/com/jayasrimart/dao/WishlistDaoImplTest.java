package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.dao.impl.WishlistDaoImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WishlistDaoImplTest {

    private static WishlistDAO wishlistDAO;
    private static ProductDAO productDAO;
    private static UserDAO userDAO;

    private static Long buyerId;
    private static Long sellerId;
    private static Long productId;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_wishlist_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        try (InputStream is = WishlistDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        wishlistDAO = new WishlistDaoImpl();
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
            stmt.execute("DELETE FROM wishlist");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
        }

        User buyer = new User();
        buyer.setName("Aarav Sharma");
        buyer.setEmail("aarav_wish@test.com");
        buyer.setPasswordHash("hash");
        buyer.setRole(Role.BUYER);
        buyerId = userDAO.create(buyer).getId();

        User seller = new User();
        seller.setName("Trend Store");
        seller.setEmail("trend@test.com");
        seller.setPasswordHash("hash");
        seller.setRole(Role.SELLER);
        sellerId = userDAO.create(seller).getId();

        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName("Noise Cancelling Headphones");
        product.setPrice(new BigDecimal("3499.00"));
        product.setStockQty(15);
        product.setCategory("Electronics");
        product.setActive(true);
        productId = productDAO.create(product).getId();
    }

    @Test
    @DisplayName("add, exists, count, and remove wishlist item")
    void testWishlistCrud() {
        assertFalse(wishlistDAO.exists(buyerId, productId));
        assertEquals(0, wishlistDAO.countByUserId(buyerId));

        boolean added = wishlistDAO.add(buyerId, productId);
        assertTrue(added);
        assertTrue(wishlistDAO.exists(buyerId, productId));
        assertEquals(1, wishlistDAO.countByUserId(buyerId));

        List<Product> products = wishlistDAO.findWishlistProducts(buyerId);
        assertEquals(1, products.size());
        assertEquals("Noise Cancelling Headphones", products.get(0).getName());
        assertEquals("Trend Store", products.get(0).getSellerName());

        List<String> categories = wishlistDAO.findInteractedCategories(buyerId);
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0));

        boolean removed = wishlistDAO.remove(buyerId, productId);
        assertTrue(removed);
        assertFalse(wishlistDAO.exists(buyerId, productId));
        assertEquals(0, wishlistDAO.countByUserId(buyerId));
    }
}
