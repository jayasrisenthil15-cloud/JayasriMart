package com.jayasrimart.controller;

import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller providing platform-wide order oversight, financial tracking, and status interventions for administrators.
 */
@WebServlet(name = "AdminOrderServlet", urlPatterns = {"/admin/orders", "/admin/orders/status"})
public class AdminOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminOrderServlet.class);

    private final AdminService adminService;

    /**
     * Default constructor.
     */
    public AdminOrderServlet() {
        this(new AdminServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param adminService the admin service
     */
    public AdminOrderServlet(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        try {
            List<Order> orders = adminService.getAllOrders();
            req.setAttribute("orders", orders);
            req.setAttribute("pageTitle", "Order Oversight - JayasriMart Admin");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/admin/admin-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error loading admin orders list: {}", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load order oversight log.");
            req.getRequestDispatcher("/WEB-INF/views/admin/admin-orders.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed during admin order update.");
            resp.sendRedirect(req.getContextPath() + "/admin/orders?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        try {
            String orderIdStr = req.getParameter("orderId");
            String statusStr = req.getParameter("status");

            if (orderIdStr == null || statusStr == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/orders?error=Order+ID+and+status+are+required.");
                return;
            }

            Long orderId = Long.parseLong(orderIdStr.trim());
            OrderStatus newStatus = OrderStatus.fromString(statusStr.trim());

            adminService.updateOrderStatus(orderId, newStatus);

            resp.sendRedirect(req.getContextPath() + "/admin/orders?success="
                    + URLEncoder.encode("Order #ORD-" + orderId + " updated to " + newStatus.name() + ".",
                    StandardCharsets.UTF_8));
        } catch (AppException e) {
            LOGGER.warn("Admin order update failed: {}", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/orders?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in AdminOrderServlet doPost: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/admin/orders?error="
                    + URLEncoder.encode("Failed to update order status.", StandardCharsets.UTF_8));
        }
    }
}
