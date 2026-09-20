package com.jayasrimart.controller;

import com.jayasrimart.dto.ApiResponse;
import com.jayasrimart.util.DBUtil;
import com.jayasrimart.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint located at {@code /api/v1/health}.
 * Verifies application runtime and database connectivity via the HikariCP pool.
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/api/v1/health")
public class HealthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LOGGER.debug("Received health check request");

        boolean isDbUp = DBUtil.checkHealth();

        Map<String, String> healthData = new LinkedHashMap<>();
        healthData.put("status", isDbUp ? "UP" : "DOWN");
        healthData.put("db", isDbUp ? "UP" : "DOWN");

        if (isDbUp) {
            ApiResponse<Map<String, String>> responseBody = ApiResponse.ok(healthData);
            JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, responseBody);
        } else {
            LOGGER.error("Health check failed: database is down or unreachable");
            ApiResponse<Map<String, String>> responseBody = new ApiResponse<>(
                    false,
                    healthData,
                    new ApiResponse.ApiError("DATABASE_UNAVAILABLE", "Database connectivity check failed", null)
            );
            JsonUtil.writeJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, responseBody);
        }
    }
}
