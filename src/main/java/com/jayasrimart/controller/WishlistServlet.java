package com.jayasrimart.controller;

import com.jayasrimart.dto.ApiResponse;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.WishlistService;
import com.jayasrimart.service.impl.WishlistServiceImpl;
import com.jayasrimart.util.CsrfUtil;
import com.jayasrimart.util.JsonUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller handling buyer wishlist view, AJAX toggling, item removals, and personalized recommendations.
 */
@WebServlet(name = "WishlistServlet", urlPatterns = {"/wishlist", "/wishlist/toggle", "/wishlist/remove"})
public class WishlistServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WishlistServlet.class);

    private final WishlistService wishlistService;

    /**
     * Default constructor.
     */
    public WishlistServlet() {
        this(new WishlistServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param wishlistService the wishlist service interface
     */
    public WishlistServlet(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            List<Product> wishlist = wishlistService.getWishlist(currentUser.getId());
            List<Product> recommendations = wishlistService.getRecommendations(currentUser.getId(), 4);

            session.setAttribute("wishlistCount", wishlist.size());
            req.setAttribute("wishlist", wishlist);
            req.setAttribute("recommendations", recommendations);
            req.setAttribute("pageTitle", "My Wishlist - JayasriMart");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/buyer/wishlist.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error loading wishlist for user {}: {}", currentUser.getId(), e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load wishlist.");
            req.getRequestDispatcher("/WEB-INF/views/buyer/wishlist.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        boolean isAjax = isAjaxRequest(req);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed during wishlist action.");
            if (isAjax) {
                sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN,
                        ApiResponse.error("CSRF_ERROR", "Security validation failed. Please refresh the page."));
            } else {
                resp.sendRedirect(req.getContextPath() + "/wishlist?error=Security+validation+failed.+Please+try+again.");
            }
            return;
        }

        String servletPath = req.getServletPath();
        String productIdStr = req.getParameter("productId");

        if (productIdStr == null || productIdStr.trim().isEmpty()) {
            if (isAjax) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("BAD_REQUEST", "Product ID is required."));
            } else {
                resp.sendRedirect(req.getContextPath() + "/wishlist?error=Product+ID+is+required.");
            }
            return;
        }

        try {
            Long productId = Long.parseLong(productIdStr.trim());

            if ("/wishlist/remove".equals(servletPath)) {
                wishlistService.removeFromWishlist(currentUser.getId(), productId);
                int count = wishlistService.getWishlistCount(currentUser.getId());
                session.setAttribute("wishlistCount", count);

                if (isAjax) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("inWishlist", false);
                    data.put("wishlistCount", count);
                    data.put("message", "Product removed from wishlist.");
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.ok(data));
                } else {
                    resp.sendRedirect(req.getContextPath() + "/wishlist?info="
                            + URLEncoder.encode("Product removed from your wishlist.", StandardCharsets.UTF_8));
                }
            } else {
                // Default: toggle
                boolean inWishlist = wishlistService.toggleWishlist(currentUser.getId(), productId);
                int count = wishlistService.getWishlistCount(currentUser.getId());
                session.setAttribute("wishlistCount", count);

                if (isAjax) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("inWishlist", inWishlist);
                    data.put("wishlistCount", count);
                    data.put("message", inWishlist ? "Product added to your wishlist!" : "Product removed from your wishlist.");
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.ok(data));
                } else {
                    String msg = inWishlist ? "Item added to your wishlist!" : "Item removed from your wishlist.";
                    String referer = req.getHeader("Referer");
                    if (referer != null && !referer.contains("/wishlist/toggle")) {
                        resp.sendRedirect(referer);
                    } else {
                        resp.sendRedirect(req.getContextPath() + "/wishlist?success="
                                + URLEncoder.encode(msg, StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (AppException e) {
            LOGGER.warn("Wishlist operation failed for user {}: {}", currentUser.getId(), e.getMessage());
            if (isAjax) {
                sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("APP_ERROR", e.getMessage()));
            } else {
                resp.sendRedirect(req.getContextPath() + "/wishlist?error="
                        + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in WishlistServlet: {}", e.getMessage(), e);
            if (isAjax) {
                sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("SERVER_ERROR", "Failed to update wishlist."));
            } else {
                resp.sendRedirect(req.getContextPath() + "/wishlist?error="
                        + URLEncoder.encode("An error occurred updating your wishlist.", StandardCharsets.UTF_8));
            }
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String requestedWith = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse<?> body)
            throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonUtil.toJson(body));
        resp.getWriter().flush();
    }
}
