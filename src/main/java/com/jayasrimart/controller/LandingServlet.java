package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.filter.AuthFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the public landing page.
 */
@WebServlet(name = "LandingServlet", urlPatterns = {"", "/index"})
public class LandingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> FEATURED_CATEGORIES = List.of(
            "Fashion", "Electronics", "Home", "Beauty", "Books", "Food", "Accessories"
    );

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);
            if (currentUser != null) {
                // Redirect logged-in users directly to their appropriate space
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

        req.setAttribute("featuredCategories", FEATURED_CATEGORIES);
        req.getRequestDispatcher("/WEB-INF/views/public/landing.jsp").forward(req, resp);
    }
}
