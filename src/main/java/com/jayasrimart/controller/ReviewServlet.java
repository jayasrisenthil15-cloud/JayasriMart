package com.jayasrimart.controller;

import com.jayasrimart.dto.ReviewRequestDTO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Order;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.OrderService;
import com.jayasrimart.service.ProductService;
import com.jayasrimart.service.ReviewService;
import com.jayasrimart.service.impl.OrderServiceImpl;
import com.jayasrimart.service.impl.ProductServiceImpl;
import com.jayasrimart.service.impl.ReviewServiceImpl;
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
 * Controller handling customer product review submissions and validation.
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/reviews", "/reviews/new"})
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServlet.class);

    private final ReviewService reviewService;
    private final ProductService productService;
    private final OrderService orderService;

    public ReviewServlet() {
        this(new ReviewServiceImpl(), new ProductServiceImpl(), new OrderServiceImpl());
    }

    public ReviewServlet(ReviewService reviewService, ProductService productService, OrderService orderService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        String productIdStr = req.getParameter("productId");
        String orderIdStr = req.getParameter("orderId");

        if (productIdStr == null || orderIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        try {
            Long productId = Long.parseLong(productIdStr.trim());
            Long orderId = Long.parseLong(orderIdStr.trim());

            boolean eligible = reviewService.isEligibleForReview(currentUser.getId(), productId, orderId);
            if (!eligible) {
                resp.sendRedirect(req.getContextPath() + "/orders/detail?id=" + orderId + "&error="
                        + URLEncoder.encode("This product is not eligible for a review or has already been reviewed.", StandardCharsets.UTF_8));
                return;
            }

            Product product = productService.getProductDetails(productId);
            Order order = orderService.getOrderDetails(currentUser.getId(), orderId, currentUser.getRole());

            req.setAttribute("product", product);
            req.setAttribute("order", order);

            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/buyer/review-form.jsp").forward(req, resp);

        } catch (AppException e) {
            LOGGER.warn("Review form error: {}", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/orders?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Error loading review form: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/orders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token verification failed during review submission.");
            resp.sendRedirect(req.getContextPath() + "/orders?error=Security+validation+failed.+Please+retry.");
            return;
        }

        String productIdStr = req.getParameter("productId");
        String orderIdStr = req.getParameter("orderId");
        String ratingStr = req.getParameter("rating");
        String comment = req.getParameter("comment");

        Long productId = null;
        Long orderId = null;
        int rating = 5;

        try {
            productId = Long.parseLong(productIdStr.trim());
            orderId = Long.parseLong(orderIdStr.trim());
            rating = (ratingStr != null && !ratingStr.trim().isEmpty()) ? Integer.parseInt(ratingStr.trim()) : 5;

            ReviewRequestDTO request = new ReviewRequestDTO(productId, orderId, rating, comment);
            reviewService.submitReview(currentUser.getId(), request);

            LOGGER.info("Review submitted successfully by user {} for product {}", currentUser.getId(), productId);
            resp.sendRedirect(req.getContextPath() + "/product?id=" + productId + "&success="
                    + URLEncoder.encode("Thank you! Your verified customer review has been published.", StandardCharsets.UTF_8));

        } catch (AppException e) {
            LOGGER.warn("Review submission rejected for user {}: {}", currentUser.getId(), e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("selectedRating", rating);
            req.setAttribute("submittedComment", comment);

            try {
                if (productId != null) {
                    req.setAttribute("product", productService.getProductDetails(productId));
                }
                if (orderId != null) {
                    req.setAttribute("order", orderService.getOrderDetails(currentUser.getId(), orderId, currentUser.getRole()));
                }
            } catch (Exception ignored) {
            }

            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/buyer/review-form.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Unexpected error submitting review: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/orders?error=An+unexpected+error+occurred.");
        }
    }
}
