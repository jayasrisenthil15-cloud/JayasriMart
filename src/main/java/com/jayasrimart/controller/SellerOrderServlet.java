package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.OrderStatus;
import com.jayasrimart.service.SellerService;
import com.jayasrimart.service.impl.SellerServiceImpl;
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
 * Controller managing incoming order fulfillment and status workflow progression for sellers.
 */
@WebServlet(name = "SellerOrderServlet", urlPatterns = {"/seller/orders", "/seller/orders/status"})
public class SellerOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerOrderServlet.class);

    private final SellerService sellerService;

    /**
     * Default constructor.
     */
    public SellerOrderServlet() {
        this(new SellerServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param sellerService the seller service interface
     */
    public SellerOrderServlet(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            List<Order> orders = sellerService.getOrders(currentUser.getId());
            req.setAttribute("orders", orders);
            req.setAttribute("pageTitle", "Incoming Orders - JayasriMart Seller");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/seller/seller-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error loading seller orders for user {}: {}",
                    currentUser.getId(), e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load incoming seller orders.");
            req.getRequestDispatcher("/WEB-INF/views/seller/seller-orders.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed during seller order status update.");
            resp.sendRedirect(req.getContextPath() + "/seller/orders?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        try {
            String orderIdStr = req.getParameter("orderId");
            String statusStr = req.getParameter("status");

            if (orderIdStr == null || statusStr == null) {
                resp.sendRedirect(req.getContextPath() + "/seller/orders?error=Order+ID+and+status+are+required.");
                return;
            }

            Long orderId = Long.parseLong(orderIdStr.trim());
            OrderStatus newStatus = OrderStatus.fromString(statusStr.trim());

            sellerService.updateOrderStatus(currentUser.getId(), orderId, newStatus);

            resp.sendRedirect(req.getContextPath() + "/seller/orders?success="
                    + URLEncoder.encode("Order #ORD-" + orderId + " updated to " + newStatus.name() + ".",
                    StandardCharsets.UTF_8));
        } catch (AppException e) {
            LOGGER.warn("Order status update failed for seller {}: {}", currentUser.getId(), e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/seller/orders?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error updating seller order status: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/seller/orders?error="
                    + URLEncoder.encode("Failed to update order status.", StandardCharsets.UTF_8));
        }
    }
}
