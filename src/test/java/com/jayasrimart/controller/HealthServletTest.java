package com.jayasrimart.controller;

import com.jayasrimart.util.DBUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HealthServlet}.
 */
@ExtendWith(MockitoExtension.class)
class HealthServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private HealthServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new HealthServlet();
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @AfterEach
    void tearDown() {
        DBUtil.closeDataSource();
    }

    @Test
    void testDoGet_DatabaseUp_ReturnsStatusOk() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:health_test_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(2);
        HikariDataSource ds = new HikariDataSource(config);
        DBUtil.initDataSource(ds);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        String jsonOutput = responseWriter.toString();
        assertTrue(jsonOutput.contains("\"success\":true"));
        assertTrue(jsonOutput.contains("\"status\":\"UP\""));
    }

    @Test
    void testDoGet_DatabaseDown_ReturnsServiceUnavailable() throws Exception {
        // Ensure pool is closed
        DBUtil.closeDataSource();

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        String jsonOutput = responseWriter.toString();
        assertTrue(jsonOutput.contains("\"success\":false"));
        assertTrue(jsonOutput.contains("DATABASE_UNAVAILABLE"));
    }
}
