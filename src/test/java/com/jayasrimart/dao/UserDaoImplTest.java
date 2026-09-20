package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.UserDaoImpl;
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
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDaoImplTest {

    private static UserDAO userDAO;

    @BeforeAll
    static void setUpAll() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test_dao;DB_CLOSE_DELAY=-1;MODE=LEGACY;DATABASE_TO_UPPER=FALSE");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        // Run schema.sql
        try (InputStream is = UserDaoImplTest.class.getClassLoader().getResourceAsStream("schema.sql");
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

        userDAO = new UserDaoImpl();
    }

    @AfterAll
    static void tearDownAll() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void cleanUsersTable() throws Exception {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reviews");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM cart_items");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
        }
    }

    @Test
    @DisplayName("Create and find user by ID and email")
    void testCreateAndFindUser() {
        User user = new User();
        user.setName("Test Buyer");
        user.setEmail("buyer@example.com");
        user.setPasswordHash("$2a$10$dummyhashvaluefortesting");
        user.setRole(Role.BUYER);

        User created = userDAO.create(user);
        assertNotNull(created.getId());
        assertEquals("Test Buyer", created.getName());
        assertEquals("buyer@example.com", created.getEmail());

        Optional<User> foundById = userDAO.findById(created.getId());
        assertTrue(foundById.isPresent());
        assertEquals(created.getId(), foundById.get().getId());
        assertEquals("Test Buyer", foundById.get().getName());

        Optional<User> foundByEmail = userDAO.findByEmail("BUYER@EXAMPLE.COM");
        assertTrue(foundByEmail.isPresent());
        assertEquals(created.getId(), foundByEmail.get().getId());
    }

    @Test
    @DisplayName("existsByEmail correctly detects existing and non-existing emails")
    void testExistsByEmail() {
        User user = new User();
        user.setName("Seller One");
        user.setEmail("seller1@example.com");
        user.setPasswordHash("$2a$10$dummyhashvaluefortesting");
        user.setRole(Role.SELLER);
        userDAO.create(user);

        assertTrue(userDAO.existsByEmail("seller1@example.com"));
        assertTrue(userDAO.existsByEmail("SELLER1@EXAMPLE.COM"));
        assertFalse(userDAO.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Update user updates fields in database")
    void testUpdateUser() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("update@example.com");
        user.setPasswordHash("$2a$10$dummyhashvaluefortesting");
        user.setRole(Role.BUYER);
        User created = userDAO.create(user);

        created.setName("New Name");
        boolean updated = userDAO.update(created);
        assertTrue(updated);

        Optional<User> fetched = userDAO.findById(created.getId());
        assertTrue(fetched.isPresent());
        assertEquals("New Name", fetched.get().getName());
    }

    @Test
    @DisplayName("List and count users by role")
    void testFindByRoleAndCount() {
        User b1 = new User();
        b1.setName("Buyer 1");
        b1.setEmail("b1@example.com");
        b1.setPasswordHash("hash");
        b1.setRole(Role.BUYER);
        userDAO.create(b1);

        User b2 = new User();
        b2.setName("Buyer 2");
        b2.setEmail("b2@example.com");
        b2.setPasswordHash("hash");
        b2.setRole(Role.BUYER);
        userDAO.create(b2);

        User s1 = new User();
        s1.setName("Seller 1");
        s1.setEmail("s1@example.com");
        s1.setPasswordHash("hash");
        s1.setRole(Role.SELLER);
        userDAO.create(s1);

        List<User> buyers = userDAO.findByRole(Role.BUYER);
        assertEquals(2, buyers.size());
        assertEquals(2, userDAO.countByRole(Role.BUYER));

        List<User> sellers = userDAO.findByRole(Role.SELLER);
        assertEquals(1, sellers.size());
        assertEquals(1, userDAO.countByRole(Role.SELLER));

        assertEquals(3, userDAO.countAll());
    }
}
