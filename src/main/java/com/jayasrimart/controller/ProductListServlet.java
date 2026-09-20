package com.jayasrimart.controller;

import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.ProductService;
import com.jayasrimart.service.impl.ProductServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller handling public and buyer product catalog browsing, multi-attribute searching,
 * category filtering, sorting, and pagination.
 */
@WebServlet(name = "ProductListServlet", urlPatterns = "/products")
public class ProductListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductListServlet.class);
    private static final int PAGE_SIZE = 12;

    private final ProductService productService;

    public ProductListServlet() {
        this(new ProductServiceImpl());
    }

    public ProductListServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = req.getParameter("q");
        String category = req.getParameter("category");
        String minPriceStr = req.getParameter("minPrice");
        String maxPriceStr = req.getParameter("maxPrice");
        String minRatingStr = req.getParameter("minRating");
        String sortBy = req.getParameter("sort");
        String pageStr = req.getParameter("page");

        BigDecimal minPrice = null;
        if (minPriceStr != null && !minPriceStr.trim().isEmpty()) {
            try {
                minPrice = new BigDecimal(minPriceStr.trim());
            } catch (NumberFormatException ignored) {
                // Ignore invalid numbers
            }
        }

        BigDecimal maxPrice = null;
        if (maxPriceStr != null && !maxPriceStr.trim().isEmpty()) {
            try {
                maxPrice = new BigDecimal(maxPriceStr.trim());
            } catch (NumberFormatException ignored) {
                // Ignore invalid numbers
            }
        }

        BigDecimal minRating = null;
        if (minRatingStr != null && !minRatingStr.trim().isEmpty()) {
            try {
                minRating = new BigDecimal(minRatingStr.trim());
            } catch (NumberFormatException ignored) {
                // Ignore invalid numbers
            }
        }

        int currentPage = 1;
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                currentPage = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {
                currentPage = 1;
            }
        }

        int offset = (currentPage - 1) * PAGE_SIZE;

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .sortBy(sortBy)
                .activeOnly(true)
                .limit(PAGE_SIZE)
                .offset(offset)
                .build();

        try {
            List<Product> products = productService.searchProducts(criteria);
            int totalCount = productService.countProducts(criteria);
            int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
            List<String> categories = productService.getAllCategories();

            req.setAttribute("products", products);
            req.setAttribute("categories", categories);
            req.setAttribute("totalCount", totalCount);
            req.setAttribute("currentPage", currentPage);
            req.setAttribute("totalPages", Math.max(1, totalPages));

            // Retain filter state in form/view
            req.setAttribute("keyword", keyword != null ? keyword.trim() : "");
            req.setAttribute("selectedCategory", category != null ? category.trim() : "");
            req.setAttribute("minPrice", minPrice);
            req.setAttribute("maxPrice", maxPrice);
            req.setAttribute("minRating", minRating);
            req.setAttribute("sortBy", sortBy != null ? sortBy.trim() : "newest");

            req.getRequestDispatcher("/WEB-INF/views/buyer/catalog.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error retrieving product catalog: {}", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to retrieve products. Please try again later.");
            req.getRequestDispatcher("/WEB-INF/views/buyer/catalog.jsp").forward(req, resp);
        }
    }
}
