package com.jayasrimart.controller;

import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.AdminService;
import com.jayasrimart.service.impl.AdminServiceImpl;
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
 * Controller handling system-wide product moderation, visibility toggles, and administrative catalog deletion.
 */
@WebServlet(name = "AdminProductServlet", urlPatterns = {
        "/admin/products",
        "/admin/products/toggle",
        "/admin/products/delete"
})
public class AdminProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProductServlet.class);

    private final AdminService adminService;

    /**
     * Default constructor.
     */
    public AdminProductServlet() {
        this(new AdminServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param adminService the admin service
     */
    public AdminProductServlet(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        try {
            List<Product> products = adminService.getAllProducts();
            req.setAttribute("products", products);
            req.setAttribute("pageTitle", "Product Moderation - JayasriMart Admin");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/admin/admin-products.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error loading admin products list: {}", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load product catalog.");
            req.getRequestDispatcher("/WEB-INF/views/admin/admin-products.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed during admin product modification.");
            resp.sendRedirect(req.getContextPath() + "/admin/products?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        String servletPath = req.getServletPath();

        try {
            String productIdStr = req.getParameter("id");
            if (productIdStr == null || productIdStr.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/products?error=Product+ID+is+required.");
                return;
            }

            Long productId = Long.parseLong(productIdStr.trim());

            if ("/admin/products/delete".equals(servletPath)) {
                adminService.deleteProduct(productId);
                resp.sendRedirect(req.getContextPath() + "/admin/products?success="
                        + URLEncoder.encode("Product #PRD-" + productId + " permanently removed.", StandardCharsets.UTF_8));
            } else {
                Product toggled = adminService.toggleProductStatus(productId);
                String state = toggled.isActive() ? "published" : "hidden/unlisted";
                resp.sendRedirect(req.getContextPath() + "/admin/products?success="
                        + URLEncoder.encode("Product #PRD-" + productId + " is now " + state + ".", StandardCharsets.UTF_8));
            }
        } catch (AppException e) {
            LOGGER.warn("Admin product action failed: {}", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/products?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in AdminProductServlet doPost: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/admin/products?error="
                    + URLEncoder.encode("Failed to process product moderation request.", StandardCharsets.UTF_8));
        }
    }
}
