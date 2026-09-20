package com.jayasrimart.controller;

import com.jayasrimart.dto.CartResponseDTO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.service.CartService;
import com.jayasrimart.service.impl.CartServiceImpl;
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
 * Controller managing buyer shopping cart operations (view, add, update, remove, and Buy Now).
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart", "/cart/add", "/cart/update", "/cart/remove"})
public class CartServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CartServlet.class);

    private final CartService cartService;

    public CartServlet() {
        this(new CartServiceImpl());
    }

    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            CartResponseDTO cart = cartService.getCart(currentUser.getId());
            session.setAttribute("cartCount", cart.getTotalItems());
            req.setAttribute("cart", cart);

            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/buyer/cart.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error displaying cart for user {}: {}", currentUser.getId(), e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load shopping cart.");
            req.getRequestDispatcher("/WEB-INF/views/buyer/cart.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF verification failed during cart modification.");
            resp.sendRedirect(req.getContextPath() + "/cart?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        String servletPath = req.getServletPath();

        try {
            if ("/cart/add".equals(servletPath)) {
                String productIdStr = req.getParameter("productId");
                String qtyStr = req.getParameter("quantity");
                String action = req.getParameter("action"); // "add" or "buyNow"

                Long productId = Long.parseLong(productIdStr.trim());
                int quantity = (qtyStr != null && !qtyStr.trim().isEmpty())
                        ? Math.max(1, Integer.parseInt(qtyStr.trim()))
                        : 1;

                CartResponseDTO updatedCart = cartService.addToCart(currentUser.getId(), productId, quantity);
                session.setAttribute("cartCount", updatedCart.getTotalItems());

                if ("buyNow".equalsIgnoreCase(action)) {
                    resp.sendRedirect(req.getContextPath() + "/checkout");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/cart?success="
                            + URLEncoder.encode("Item added to your cart!", StandardCharsets.UTF_8));
                }

            } else if ("/cart/update".equals(servletPath)) {
                String productIdStr = req.getParameter("productId");
                String qtyStr = req.getParameter("quantity");

                Long productId = Long.parseLong(productIdStr.trim());
                int quantity = Integer.parseInt(qtyStr.trim());

                CartResponseDTO updatedCart = cartService.updateCartItem(currentUser.getId(), productId, quantity);
                session.setAttribute("cartCount", updatedCart.getTotalItems());

                resp.sendRedirect(req.getContextPath() + "/cart");

            } else if ("/cart/remove".equals(servletPath)) {
                String productIdStr = req.getParameter("productId");
                Long productId = Long.parseLong(productIdStr.trim());

                CartResponseDTO updatedCart = cartService.removeFromCart(currentUser.getId(), productId);
                session.setAttribute("cartCount", updatedCart.getTotalItems());

                resp.sendRedirect(req.getContextPath() + "/cart?info="
                        + URLEncoder.encode("Item removed from your cart.", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(req.getContextPath() + "/cart");
            }
        } catch (AppException e) {
            LOGGER.warn("Cart operation failed for user {}: {}", currentUser.getId(), e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/cart?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in CartServlet: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/cart?error="
                    + URLEncoder.encode("An error occurred updating your cart.", StandardCharsets.UTF_8));
        }
    }
}
