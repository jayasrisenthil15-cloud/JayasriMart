package com.jayasrimart.controller;

import com.jayasrimart.dto.CartResponseDTO;
import com.jayasrimart.dto.CheckoutRequestDTO;
import com.jayasrimart.dto.OrderResponseDTO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.PaymentMethod;
import com.jayasrimart.service.CartService;
import com.jayasrimart.service.OrderService;
import com.jayasrimart.service.impl.CartServiceImpl;
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

/**
 * Controller managing the checkout workflow, delivery destination input, mock payment processing,
 * and order creation.
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/checkout")
public class CheckoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutServlet.class);

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutServlet() {
        this(new CartServiceImpl(), new OrderServiceImpl());
    }

    public CheckoutServlet(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        CartResponseDTO cart = cartService.getCart(currentUser.getId());
        if (cart.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart?info=Your+shopping+cart+is+empty.");
            return;
        }

        req.setAttribute("cart", cart);
        req.setAttribute("defaultName", currentUser.getName());

        CsrfUtil.getToken(session);
        req.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF verification failed during checkout for user {}", currentUser.getId());
            req.setAttribute("errorMessage", "Security validation failed. Please refresh and try again.");
            doGet(req, resp);
            return;
        }

        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String city = req.getParameter("city");
        String pincode = req.getParameter("pincode");
        String paymentMethodStr = req.getParameter("paymentMethod");
        String upiId = req.getParameter("upiId");
        String cardNumber = req.getParameter("cardNumber");
        String cardExpiry = req.getParameter("cardExpiry");
        String cardCvv = req.getParameter("cardCvv");

        PaymentMethod paymentMethod = PaymentMethod.fromString(paymentMethodStr);

        CheckoutRequestDTO checkoutRequest = CheckoutRequestDTO.builder()
                .name(name)
                .phone(phone)
                .address(address)
                .city(city)
                .pincode(pincode)
                .paymentMethod(paymentMethod)
                .upiId(upiId)
                .cardNumber(cardNumber)
                .cardExpiry(cardExpiry)
                .cardCvv(cardCvv)
                .build();

        try {
            OrderResponseDTO createdOrder = orderService.placeOrder(currentUser.getId(), checkoutRequest);

            // Reset session cart count
            session.setAttribute("cartCount", 0);

            LOGGER.info("Order successfully placed ID {} for user {}", createdOrder.getId(), currentUser.getId());
            resp.sendRedirect(req.getContextPath() + "/order/success?id=" + createdOrder.getId());

        } catch (AppException e) {
            LOGGER.warn("Checkout rejected for user {}: {}", currentUser.getId(), e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("checkoutData", checkoutRequest);
            req.setAttribute("cart", cartService.getCart(currentUser.getId()));
            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Unexpected checkout error for user {}: {}", currentUser.getId(), e.getMessage(), e);
            req.setAttribute("errorMessage", "An unexpected error occurred during checkout. Please try again.");
            req.setAttribute("checkoutData", checkoutRequest);
            req.setAttribute("cart", cartService.getCart(currentUser.getId()));
            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/buyer/checkout.jsp").forward(req, resp);
        }
    }
}
