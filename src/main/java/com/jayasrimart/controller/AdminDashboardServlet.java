package com.jayasrimart.controller;

import com.jayasrimart.dto.AdminDashboardDTO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.service.AdminService;
import com.jayasrimart.service.impl.AdminServiceImpl;
import com.jayasrimart.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controller serving the primary administrator overview dashboard with platform KPIs and analytics.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminDashboardServlet.class);

    private final AdminService adminService;

    /**
     * Default constructor.
     */
    public AdminDashboardServlet() {
        this(new AdminServiceImpl());
    }

    /**
     * Parameterized constructor for testing.
     *
     * @param adminService the admin service
     */
    public AdminDashboardServlet(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            AdminDashboardDTO dashboard = adminService.getDashboardData();
            req.setAttribute("dashboard", dashboard);
            req.setAttribute("pageTitle", "Admin Console - JayasriMart");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/admin/admin-dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error displaying admin dashboard for user {}: {}",
                    currentUser != null ? currentUser.getId() : "unknown", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load platform analytics.");
            req.getRequestDispatcher("/WEB-INF/views/admin/admin-dashboard.jsp").forward(req, resp);
        }
    }
}
