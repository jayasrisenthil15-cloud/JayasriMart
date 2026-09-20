package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.UserDaoImpl;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.PasswordUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
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

class UserDaoTest {

    private static HikariDataSource testDataSource;
    private UserDAO userDAO;

    @BeforeAll
    static void initDatabase() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setPoolName("UserDaoTestPool");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:user_dao_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        testDataSource = new HikariDataSource(config);
        DBUtil.initDataSource(testDataSource);

        runScript("schema.sql");
        runScript("seed.sql");
    }

    @AfterAll
    static void destroyDatabase() {
        DBUtil.closeDataSource();
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDaoImpl();
    }

    private static void runScript(String resourcePath) throws Exception {
        try (InputStream is = UserDaoTest.class.getClassLoader().getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1).trim();
                    }
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                    sqlBuilder.setLength(0);
                }
            }
        }
    }

    @Test
    @DisplayName("Find user by email returns populated user")
    void testFindByEmail() {
        Optional<User> userOpt = userDAO.findByEmail("admin@jayasrimart.com");
        assertTrue(userOpt.isPresent());
        assertEquals("System Administrator", userOpt.get().getName());
        assertEquals(Role.ADMIN, userOpt.get().getRole());
    }

    @Test
    @DisplayName("Exists by email correctly identifies existing and non-existing emails")
    void testExistsByEmail() {
        assertTrue(userDAO.existsByEmail("buyer1@jayasrimart.com"));
        assertTrue(userDAO.existsByEmail("BUYER1@JAYASRIMART.COM")); // Case insensitive
        assertFalse(userDAO.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Create user inserts new user and sets generated ID")
    void testCreateUser() {
        User user = new User();
        user.setName("New Customer");
        user.setEmail("newcustomer@test.com");
        user.setPasswordHash(PasswordUtil.hash("Customer@123"));
        user.setRole(Role.BUYER);

        User created = userDAO.create(user);
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());

        Optional<User> fetched = userDAO.findById(created.getId());
        assertTrue(fetched.isPresent());
        assertEquals("New Customer", fetched.get().getName());
        assertEquals(Role.BUYER, fetched.get().getRole());
    }

    @Test
    @DisplayName("Find by role retrieves correct counts and roles")
    void testFindByRole() {
        List<User> sellers = userDAO.findByRole(Role.SELLER);
        assertTrue(sellers.size() >= 2);
        for (User u : sellers) {
            assertEquals(Role.SELLER, u.getRole());
        }

        int buyerCount = userDAO.countByRole(Role.BUYER);
        assertTrue(buyerCount >= 2);
    }

    @Test
    @DisplayName("Update user modifies name successfully")
    void testUpdateUser() {
        Optional<User> userOpt = userDAO.findByEmail("seller1@jayasrimart.com");
        assertTrue(userOpt.isPresent());

        User user = userOpt.get();
        user.setName("Apex Trends Store");
        boolean updated = userDAO.update(user);
        assertTrue(updated);

        Optional<User> reloaded = userDAO.findById(user.getId());
        assertTrue(reloaded.isPresent());
        assertEquals("Apex Trends Store", reloaded.get().getName());
    }
}
