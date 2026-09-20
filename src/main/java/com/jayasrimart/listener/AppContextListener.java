package com.jayasrimart.listener;

import com.jayasrimart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Core application lifecycle listener.
 * <p>
 * Responsibilities:
 * 1. Initializes the HikariCP connection pool on application startup.
 * 2. Executes schema.sql and seed.sql to provision the database.
 * 3. Safely closes the HikariCP connection pool on application shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);
    private static final String CONFIG_FILE = "config.properties";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Starting JayasriMart Application initialization...");
        long startTime = System.currentTimeMillis();

        try {
            Properties config = loadConfiguration();
            HikariDataSource dataSource = createDataSource(config);
            DBUtil.initDataSource(dataSource);

            // Execute schema and seed migrations
            initDatabase();

            long elapsed = System.currentTimeMillis() - startTime;
            LOGGER.info("JayasriMart Application initialized successfully in {} ms.", elapsed);
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Failed to initialize application context: {}", e.getMessage(), e);
            throw new RuntimeException("Application startup failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down JayasriMart Application context...");
        DBUtil.closeDataSource();
        LOGGER.info("JayasriMart Application context destroyed cleanly.");
    }

    private Properties loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                LOGGER.info("Loaded configuration properties from classpath: {}", CONFIG_FILE);
            } else {
                LOGGER.warn("Configuration file '{}' not found on classpath. Falling back to defaults / env vars.", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read configuration file '{}'. Using environment defaults.", CONFIG_FILE, e);
        }
        return props;
    }

    private HikariDataSource createDataSource(Properties config) {
        String dbUrl = getEnvOrProp("DB_URL", "db.url", config, "jdbc:h2:file:./data/jayasrimart;AUTO_SERVER=TRUE");
        String dbUser = getEnvOrProp("DB_USER", "db.user", config, "sa");
        String dbPassword = getEnvOrProp("DB_PASSWORD", "db.password", config, "");
        int maxPoolSize = Integer.parseInt(getEnvOrProp("DB_MAX_POOL_SIZE", "db.pool.maxSize", config, "10"));

        LOGGER.info("Configuring HikariCP DataSource: URL='{}', User='{}', MaxPoolSize={}", dbUrl, dbUser, maxPoolSize);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("JayasriMartHikariPool");
        hikariConfig.setDriverClassName("org.h2.Driver");
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(dbUser);
        hikariConfig.setPassword(dbPassword);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setMinimumIdle(Integer.parseInt(config.getProperty("db.pool.minIdle", "2")));
        hikariConfig.setConnectionTimeout(Long.parseLong(config.getProperty("db.pool.connectionTimeout", "30000")));
        hikariConfig.setIdleTimeout(Long.parseLong(config.getProperty("db.pool.idleTimeout", "600000")));
        hikariConfig.setMaxLifetime(Long.parseLong(config.getProperty("db.pool.maxLifetime", "1800000")));

        return new HikariDataSource(hikariConfig);
    }

    private String getEnvOrProp(String envKey, String propKey, Properties props, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        String sysProp = System.getProperty(propKey);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }
        String propVal = props.getProperty(propKey);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }
        return defaultValue;
    }

    private void initDatabase() throws SQLException, IOException {
        LOGGER.info("Executing database schema initialization (schema.sql)...");
        executeSqlScript("schema.sql");

        if (isSeedRequired()) {
            LOGGER.info("Users table is empty. Executing seed data script (seed.sql)...");
            executeSqlScript("seed.sql");
            LOGGER.info("Database seed data populated successfully.");
        } else {
            LOGGER.info("Database already contains data; skipping seed.sql execution.");
        }
    }

    private boolean isSeedRequired() {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM users")) {
            if (rs.next()) {
                return rs.getInt("total") == 0;
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not determine user count for seeding check: {}. Will attempt seeding.", e.getMessage());
            return true;
        }
        return false;
    }

    private void executeSqlScript(String resourcePath) throws SQLException, IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("SQL resource not found on classpath: " + resourcePath);
            }
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
                        // Remove trailing semicolon
                        if (sql.endsWith(";")) {
                            sql = sql.substring(0, sql.length() - 1).trim();
                        }
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                        sqlBuilder.setLength(0);
                    }
                }
                // Execute remaining statement if any
                String remaining = sqlBuilder.toString().trim();
                if (!remaining.isEmpty()) {
                    if (remaining.endsWith(";")) {
                        remaining = remaining.substring(0, remaining.length() - 1).trim();
                    }
                    if (!remaining.isEmpty()) {
                        stmt.execute(remaining);
                    }
                }
            }
        }
    }
}
