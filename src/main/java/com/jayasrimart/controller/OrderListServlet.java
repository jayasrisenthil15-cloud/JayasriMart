package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Order;
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
import java.util.List;

/**
 * Controller displaying buyer order history and order tracking details.
 */
@WebServlet(name = "OrderListServlet", urlPatterns = {"/orders", "/orders/detail"})
public class OrderListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderListServlet.class);

    private final OrderService orderService;

    public OrderListServlet() {
        this(new OrderServiceImpl());
    }

    public OrderListServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);
        String servletPath = req.getServletPath();

        try {
            if ("/orders/detail".equals(servletPath)) {
                String idParam = req.getParameter("id");
                if (idParam == null || idParam.trim().isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/orders");
                    return;
                }

                Long orderId = Long.parseLong(idParam.trim());
                Order order = orderService.getOrderDetails(currentUser.getId(), orderId, currentUser.getRole());

                req.setAttribute("order", order);
                CsrfUtil.getToken(session);
                req.getRequestDispatcher("/WEB-INF/views/buyer/order-detail.jsp").forward(req, resp);

            } else {
                List<Order> orders = orderService.getBuyerOrders(currentUser.getId());
                req.setAttribute("orders", orders);
                req.getRequestDispatcher("/WEB-INF/views/buyer/orders.jsp").forward(req, resp);
            }
        } catch (AppException e) {
            LOGGER.warn("Order lookup failed for user {}: {}", currentUser.getId(), e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders?error=" + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error retrieving orders for user {}: {}", currentUser.getId(), e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/orders?error=Failed+to+retrieve+order+details.");
        }
    }
}
