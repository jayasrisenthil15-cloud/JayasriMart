package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.ProductService;
import com.jayasrimart.service.impl.ProductServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the public landing page showcasing featured categories and top-rated products.
 */
@WebServlet(name = "LandingServlet", urlPatterns = {"", "/index"})
public class LandingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ProductService productService;

    public LandingServlet() {
        this(new ProductServiceImpl());
    }

    public LandingServlet(ProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);
            if (currentUser != null) {
                switch (currentUser.getRole()) {
                    case SELLER -> {
                        resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
                        return;
                    }
                    case ADMIN -> {
                        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                        return;
                    }
                    default -> {
                        resp.sendRedirect(req.getContextPath() + "/products");
                        return;
                    }
                }
            }
        }

        List<String> categories = productService.getAllCategories();
        List<Product> featuredProducts = productService.getFeaturedProducts(8);

        req.setAttribute("featuredCategories", categories);
        req.setAttribute("featuredProducts", featuredProducts);

        req.getRequestDispatcher("/WEB-INF/views/public/landing.jsp").forward(req, resp);
    }
}
