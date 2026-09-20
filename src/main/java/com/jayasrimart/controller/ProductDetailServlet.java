package com.jayasrimart.controller;

import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Review;
import com.jayasrimart.service.ProductService;
import com.jayasrimart.service.impl.ProductServiceImpl;
import com.jayasrimart.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Controller providing full details, verified customer reviews, seller attribution,
 * and purchase controls for an individual product.
 */
@WebServlet(name = "ProductDetailServlet", urlPatterns = {"/product", "/products/detail"})
public class ProductDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDetailServlet.class);

    private final ProductService productService;

    public ProductDetailServlet() {
        this(new ProductServiceImpl());
    }

    public ProductDetailServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        try {
            Long productId = Long.parseLong(idParam.trim());
            Product product = productService.getProductDetails(productId);
            List<Review> reviews = productService.getProductReviews(productId);

            req.setAttribute("product", product);
            req.setAttribute("reviews", reviews);

            // Make sure CSRF token is available for quick Add-to-Cart form
            CsrfUtil.getToken(req.getSession(true));

            req.getRequestDispatcher("/WEB-INF/views/buyer/product-detail.jsp").forward(req, resp);

        } catch (NumberFormatException | ResourceNotFoundException e) {
            LOGGER.warn("Product detail lookup failed for id '{}': {}", idParam, e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested product was not found.");
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading product details for id {}: {}", idParam, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while loading the product.");
        }
    }
}
