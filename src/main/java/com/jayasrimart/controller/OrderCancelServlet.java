package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.service.OrderService;
import com.jayasrimart.service.impl.OrderServiceImpl;
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

/**
 * Controller handling order cancellation by buyers.
 */
@WebServlet(name = "OrderCancelServlet", urlPatterns = "/orders/cancel")
public class OrderCancelServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCancelServlet.class);

    private final OrderService orderService;

    public OrderCancelServlet() {
        this(new OrderServiceImpl());
    }

    public OrderCancelServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token verification failed during order cancellation attempt.");
            resp.sendRedirect(req.getContextPath() + "/orders?error=Security+validation+failed.");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdStr.trim());
            orderService.cancelOrder(currentUser.getId(), orderId);

            LOGGER.info("User {} successfully cancelled order {}", currentUser.getId(), orderId);
            resp.sendRedirect(req.getContextPath() + "/orders/detail?id=" + orderId + "&success="
                    + URLEncoder.encode("Order cancelled successfully. Inventory has been restocked.", StandardCharsets.UTF_8));

        } catch (AppException e) {
            LOGGER.warn("Cancellation failed for order '{}': {}", orderIdStr, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders/detail?id=" + orderIdStr + "&error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error cancelling order '{}': {}", orderIdStr, e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/orders?error=An+unexpected+error+occurred.");
        }
    }
}
