package com.jayasrimart;

import com.jayasrimart.dto.ApiResponse;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.JsonUtil;
import com.jayasrimart.util.PasswordUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhaseOneTest {

    private static HikariDataSource testDataSource;

    @BeforeAll
    static void setUpDatabase() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setPoolName("TestH2Pool");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        testDataSource = new HikariDataSource(config);
        DBUtil.initDataSource(testDataSource);

        // Execute schema.sql and seed.sql
        runScript("schema.sql");
        runScript("seed.sql");
    }

    @AfterAll
    static void tearDownDatabase() {
        DBUtil.closeDataSource();
    }

    private static void runScript(String resourceName) throws Exception {
        try (InputStream is = PhaseOneTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(is, "Resource script " + resourceName + " must exist on classpath");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
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
    }

    @Test
    @DisplayName("DBUtil health check returns true on active connection pool")
    void testDbHealthCheck() {
        assertTrue(DBUtil.checkHealth(), "Database health check should return true");
    }

    @Test
    @DisplayName("Schema and seed scripts correctly populate all 5 users")
    void testSeededUsers() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
            assertTrue(rs.next());
            assertEquals(5, rs.getInt("total"), "Expected 5 seeded users (1 admin, 2 sellers, 2 buyers)");
        }
    }

    @Test
    @DisplayName("Schema and seed scripts correctly populate 24 products across categories")
    void testSeededProducts() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM products")) {
            assertTrue(rs.next());
            assertEquals(24, rs.getInt("total"), "Expected 24 seeded products");
        }
    }

    @Test
    @DisplayName("Schema and seed scripts correctly populate sample orders, items, and reviews")
    void testSeededOrdersAndReviews() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM orders")) {
                assertTrue(rs.next());
                assertEquals(3, rs.getInt("total"), "Expected 3 seeded orders");
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM order_items")) {
                assertTrue(rs.next());
                assertEquals(4, rs.getInt("total"), "Expected 4 seeded order items");
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM reviews")) {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("total"), "Expected 2 seeded reviews");
            }
        }
    }

    @Test
    @DisplayName("PasswordUtil correctly verifies BCrypt hashes for seeded accounts")
    void testPasswordVerification() throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email, password_hash FROM users")) {

            while (rs.next()) {
                String email = rs.getString("email");
                String hash = rs.getString("password_hash");

                if ("admin@jayasrimart.com".equals(email)) {
                    assertTrue(PasswordUtil.verify("Admin@123", hash), "Admin password verification failed");
                    assertFalse(PasswordUtil.verify("WrongPass", hash));
                } else if (email.startsWith("seller")) {
                    assertTrue(PasswordUtil.verify("Seller@123", hash), "Seller password verification failed");
                } else if (email.startsWith("buyer")) {
                    assertTrue(PasswordUtil.verify("Buyer@123", hash), "Buyer password verification failed");
                }
            }
        }
    }

    @Test
    @DisplayName("ApiResponse serializes properly to JSON with standard envelope")
    void testApiResponseSerialization() {
        ApiResponse<String> successResponse = ApiResponse.ok("System Healthy");
        String json = JsonUtil.toJson(successResponse);
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":\"System Healthy\""));

        ApiResponse<Void> errorResponse = ApiResponse.error("BAD_REQUEST", "Invalid field", "email");
        String errorJson = JsonUtil.toJson(errorResponse);
        assertTrue(errorJson.contains("\"success\":false"));
        assertTrue(errorJson.contains("\"code\":\"BAD_REQUEST\""));
        assertTrue(errorJson.contains("\"field\":\"email\""));
    }
}
