package com.jayasrimart.controller;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.SellerService;
import com.jayasrimart.service.impl.SellerServiceImpl;
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
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller handling product management (catalog view, creation, editing, deletion) for sellers.
 */
@WebServlet(name = "SellerProductServlet", urlPatterns = {
        "/seller/products",
        "/seller/products/new",
        "/seller/products/edit",
        "/seller/products/delete"
})
public class SellerProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerProductServlet.class);

    private final SellerService sellerService;
    private final ProductDAO productDAO;

    /**
     * Default constructor.
     */
    public SellerProductServlet() {
        this(new SellerServiceImpl(), DaoFactory.getProductDAO());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param sellerService the seller service
     * @param productDAO the product DAO for category list
     */
    public SellerProductServlet(SellerService sellerService, ProductDAO productDAO) {
        this.sellerService = sellerService;
        this.productDAO = productDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);
        String path = req.getServletPath();

        CsrfUtil.getToken(session);

        try {
            if ("/seller/products/new".equals(path)) {
                List<String> categories = productDAO.findAllCategories();
                req.setAttribute("categories", categories);
                req.setAttribute("pageTitle", "Add New Product - JayasriMart Seller");
                req.getRequestDispatcher("/WEB-INF/views/seller/seller-product-form.jsp").forward(req, resp);

            } else if ("/seller/products/edit".equals(path)) {
                String idStr = req.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/seller/products?error=Product+ID+is+required.");
                    return;
                }
                Long productId = Long.parseLong(idStr.trim());
                Product product = sellerService.getProduct(currentUser.getId(), productId);

                List<String> categories = productDAO.findAllCategories();
                req.setAttribute("product", product);
                req.setAttribute("categories", categories);
                req.setAttribute("isEdit", true);
                req.setAttribute("pageTitle", "Edit Product - " + product.getName());
                req.getRequestDispatcher("/WEB-INF/views/seller/seller-product-form.jsp").forward(req, resp);

            } else {
                // Default: /seller/products list
                List<Product> products = sellerService.getProducts(currentUser.getId());
                req.setAttribute("products", products);
                req.setAttribute("pageTitle", "My Products - JayasriMart Seller");
                req.getRequestDispatcher("/WEB-INF/views/seller/seller-products.jsp").forward(req, resp);
            }
        } catch (AppException e) {
            LOGGER.warn("Seller product operation failed for user {}: {}", currentUser.getId(), e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/seller/products?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in SellerProductServlet doGet: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/seller/products?error="
                    + URLEncoder.encode("An error occurred while loading products.", StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed for seller product modification.");
            resp.sendRedirect(req.getContextPath() + "/seller/products?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        String path = req.getServletPath();

        try {
            if ("/seller/products/delete".equals(path)) {
                String idStr = req.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/seller/products?error=Product+ID+is+required.");
                    return;
                }
                Long productId = Long.parseLong(idStr.trim());
                sellerService.deleteProduct(currentUser.getId(), productId);

                resp.sendRedirect(req.getContextPath() + "/seller/products?success="
                        + URLEncoder.encode("Product successfully removed from your catalog.", StandardCharsets.UTF_8));

            } else if ("/seller/products/edit".equals(path)) {
                String idStr = req.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/seller/products?error=Product+ID+is+required.");
                    return;
                }
                Long productId = Long.parseLong(idStr.trim());

                Product product = extractProductFromRequest(req);
                product.setId(productId);

                sellerService.updateProduct(currentUser.getId(), product);

                resp.sendRedirect(req.getContextPath() + "/seller/products?success="
                        + URLEncoder.encode("Product '" + product.getName() + "' updated successfully!", StandardCharsets.UTF_8));

            } else {
                // Create new product (/seller/products/new)
                Product product = extractProductFromRequest(req);
                Product created = sellerService.createProduct(currentUser.getId(), product);

                resp.sendRedirect(req.getContextPath() + "/seller/products?success="
                        + URLEncoder.encode("Product '" + created.getName() + "' created and listed successfully!",
                        StandardCharsets.UTF_8));
            }
        } catch (AppException e) {
            LOGGER.warn("Seller product modification failed for user {}: {}", currentUser.getId(), e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/seller/products?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in SellerProductServlet doPost: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/seller/products?error="
                    + URLEncoder.encode("Failed to save product details.", StandardCharsets.UTF_8));
        }
    }

    private Product extractProductFromRequest(HttpServletRequest req) {
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stockQty");
        String category = req.getParameter("category");
        String customCategory = req.getParameter("customCategory");
        String imageUrl = req.getParameter("imageUrl");
        String activeStr = req.getParameter("active");

        if ("Other".equalsIgnoreCase(category) && customCategory != null && !customCategory.trim().isEmpty()) {
            category = customCategory.trim();
        }

        BigDecimal price = (priceStr != null && !priceStr.trim().isEmpty())
                ? new BigDecimal(priceStr.trim())
                : BigDecimal.ZERO;

        int stockQty = (stockStr != null && !stockStr.trim().isEmpty())
                ? Integer.parseInt(stockStr.trim())
                : 0;

        boolean active = "on".equalsIgnoreCase(activeStr) || "true".equalsIgnoreCase(activeStr);

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setActive(active);

        return product;
    }
}
