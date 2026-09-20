package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.dto.ProductSearchCriteria;
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

class ProductDaoImplTest {

    private static ProductDAO productDAO;
    private static UserDAO userDAO;
    private static Long sellerId;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_product_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        // Run schema.sql
        try (InputStream is = ProductDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        productDAO = new ProductDaoImpl();
        userDAO = new UserDaoImpl();
    }

    @AfterAll
    static void tearDownAll() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void cleanTablesAndSeedSeller() throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reviews");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM cart_items");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
        }

        User seller = new User();
        seller.setName("Apex Trends");
        seller.setEmail("apex@example.com");
        seller.setPasswordHash("$2a$10$dummyhash");
        seller.setRole(Role.SELLER);
        User createdSeller = userDAO.create(seller);
        sellerId = createdSeller.getId();
    }

    private Product createSampleProduct(String name, String category, BigDecimal price, BigDecimal avgRating, int stock) {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription(name + " detailed description");
        p.setCategory(category);
        p.setPrice(price);
        p.setAvgRating(avgRating);
        p.setStockQty(stock);
        p.setImageUrl("https://example.com/img.jpg");
        p.setActive(true);
        return productDAO.create(p);
    }

    @Test
    @DisplayName("Create product and find by ID with seller name")
    void testCreateAndFindById() {
        Product created = createSampleProduct("Cotton Shirt", "Fashion", new BigDecimal("999.00"), new BigDecimal("4.80"), 20);
        assertNotNull(created.getId());

        Optional<Product> found = productDAO.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Cotton Shirt", found.get().getName());
        assertEquals("Apex Trends", found.get().getSellerName());
        assertEquals(0, new BigDecimal("999.00").compareTo(found.get().getPrice()));
    }

    @Test
    @DisplayName("Combined search query filters correctly by keyword, category, price range, and min rating")
    void testSearchAndFilterCombined() {
        createSampleProduct("Blue Cotton Oxford Shirt", "Fashion", new BigDecimal("1200.00"), new BigDecimal("4.50"), 10);
        createSampleProduct("Red Silk Party Dress", "Fashion", new BigDecimal("2500.00"), new BigDecimal("4.80"), 5);
        createSampleProduct("Wireless Noise Cancelling Headphones", "Electronics", new BigDecimal("4999.00"), new BigDecimal("4.20"), 15);
        createSampleProduct("Casual Running Shoes", "Fashion", new BigDecimal("800.00"), new BigDecimal("3.50"), 20);

        // Search Fashion + Price 1000 to 3000 + Rating >= 4.0
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .category("Fashion")
                .minPrice(new BigDecimal("1000.00"))
                .maxPrice(new BigDecimal("3000.00"))
                .minRating(new BigDecimal("4.00"))
                .sortBy("price_asc")
                .build();

        List<Product> results = productDAO.searchAndFilter(criteria);
        assertEquals(2, results.size());
        assertEquals("Blue Cotton Oxford Shirt", results.get(0).getName());
        assertEquals("Red Silk Party Dress", results.get(1).getName());

        int count = productDAO.countSearchAndFilter(criteria);
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Search by keyword matches name or description case-insensitively")
    void testSearchByKeyword() {
        createSampleProduct("Sony Wireless Headphones", "Electronics", new BigDecimal("3999.00"), new BigDecimal("4.50"), 10);
        createSampleProduct("Gaming Mechanical Keyboard", "Electronics", new BigDecimal("2999.00"), new BigDecimal("4.60"), 8);

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword("wireless")
                .build();

        List<Product> results = productDAO.searchAndFilter(criteria);
        assertEquals(1, results.size());
        assertEquals("Sony Wireless Headphones", results.get(0).getName());
    }

    @Test
    @DisplayName("findFeatured returns top-rated products limited to requested size")
    void testFindFeatured() {
        createSampleProduct("P1", "Cat1", new BigDecimal("100.00"), new BigDecimal("3.00"), 5);
        createSampleProduct("P2", "Cat1", new BigDecimal("200.00"), new BigDecimal("5.00"), 5);
        createSampleProduct("P3", "Cat1", new BigDecimal("300.00"), new BigDecimal("4.50"), 5);

        List<Product> featured = productDAO.findFeatured(2);
        assertEquals(2, featured.size());
        assertEquals("P2", featured.get(0).getName());
        assertEquals("P3", featured.get(1).getName());
    }

    @Test
    @DisplayName("updateStock reduces stock atomically inside connection and rejects overdraft")
    void testUpdateStock() throws Exception {
        Product p = createSampleProduct("Limited Edition Watch", "Accessories", new BigDecimal("1500.00"), BigDecimal.ZERO, 5);

        try (Connection conn = DBUtil.getConnection()) {
            boolean success = productDAO.updateStock(p.getId(), 3, conn);
            assertTrue(success);

            Optional<Product> updated = productDAO.findById(p.getId());
            assertEquals(2, updated.get().getStockQty());

            // Overdraft attempt: try to deduct 5 when only 2 remain
            boolean overdraft = productDAO.updateStock(p.getId(), 5, conn);
            assertFalse(overdraft);
        }
    }

    @Test
    @DisplayName("findAllCategories returns distinct list of categories")
    void testFindAllCategories() {
        createSampleProduct("Item 1", "Books", new BigDecimal("500.00"), BigDecimal.ZERO, 10);
        createSampleProduct("Item 2", "Books", new BigDecimal("600.00"), BigDecimal.ZERO, 10);
        createSampleProduct("Item 3", "Home", new BigDecimal("700.00"), BigDecimal.ZERO, 10);

        List<String> categories = productDAO.findAllCategories();
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Books"));
        assertTrue(categories.contains("Home"));
    }
}
