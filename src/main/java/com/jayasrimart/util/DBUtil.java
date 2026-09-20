package com.jayasrimart.util;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class providing managed JDBC database connections from the application's
 * HikariCP connection pool.
 * <p>
 * Follows the Singleton pattern for connection pool management. Direct instantiation of
 * {@link java.sql.DriverManager} is prohibited. Connections must only be acquired from this utility.
 */
public final class DBUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);
    private static volatile HikariDataSource dataSource;

    private DBUtil() {
        // Utility class; prevent instantiation
    }

    /**
     * Initializes the connection pool with the provided {@link HikariDataSource}.
     * This method is invoked exclusively by {@link com.jayasrimart.listener.AppContextListener}
     * during application startup.
     *
     * @param ds the configured HikariDataSource instance
     */
    public static synchronized void initDataSource(HikariDataSource ds) {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.warn("HikariDataSource is already initialized. Replacing existing pool.");
            dataSource.close();
        }
        dataSource = ds;
        LOGGER.info("HikariDataSource successfully registered with DBUtil.");
    }

    /**
     * Retrieves an active JDBC {@link Connection} from the pool.
     * Callers must manage the connection lifecycle using try-with-resources.
     *
     * @return an active {@link Connection}
     * @throws SQLException if a database access error occurs or pool is exhausted
     * @throws IllegalStateException if the connection pool has not been initialized
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("HikariCP connection pool has not been initialized or is closed.");
        }
        return dataSource.getConnection();
    }

    /**
     * Returns the underlying {@link DataSource}.
     *
     * @return the active {@link DataSource}
     */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Checks database connectivity by executing a lightweight test query.
     *
     * @return true if database connection and query execution succeed, false otherwise
     */
    public static boolean checkHealth() {
        if (dataSource == null || dataSource.isClosed()) {
            return false;
        }
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (SQLException e) {
            LOGGER.error("Database health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Closes the underlying {@link HikariDataSource} pool cleanly.
     * Invoked during application shutdown by {@link com.jayasrimart.listener.AppContextListener}.
     */
    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Closing HikariCP connection pool...");
            dataSource.close();
            dataSource = null;
            LOGGER.info("HikariCP connection pool closed successfully.");
        }
    }
}
