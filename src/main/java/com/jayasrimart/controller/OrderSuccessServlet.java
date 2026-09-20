package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Order;
import com.jayasrimart.service.OrderService;
import com.jayasrimart.service.impl.OrderServiceImpl;
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
 * Controller rendering the post-checkout order confirmation and success view.
 */
@WebServlet(name = "OrderSuccessServlet", urlPatterns = "/order/success")
public class OrderSuccessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSuccessServlet.class);

    private final OrderService orderService;

    public OrderSuccessServlet() {
        this(new OrderServiceImpl());
    }

    public OrderSuccessServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        try {
            Long orderId = Long.parseLong(idParam.trim());
            Order order = orderService.getOrderDetails(currentUser.getId(), orderId, currentUser.getRole());

            req.setAttribute("order", order);
            req.getRequestDispatcher("/WEB-INF/views/buyer/order-success.jsp").forward(req, resp);

        } catch (AppException e) {
            LOGGER.warn("Order success lookup failed: {}", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders?error=" + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error retrieving order confirmation: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/orders");
        }
    }
}
