package com.jayasrimart.controller;

import com.jayasrimart.dto.SellerDashboardDTO;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.filter.AuthFilter;
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

/**
 * Controller serving the Seller Portal dashboard with store overview and key performance metrics.
 */
@WebServlet(name = "SellerDashboardServlet", urlPatterns = {"/seller", "/seller/dashboard"})
public class SellerDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SellerDashboardServlet.class);

    private final SellerService sellerService;

    /**
     * Default constructor.
     */
    public SellerDashboardServlet() {
        this(new SellerServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param sellerService the seller service interface
     */
    public SellerDashboardServlet(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            SellerDashboardDTO dashboard = sellerService.getDashboardData(currentUser.getId());
            req.setAttribute("dashboard", dashboard);
            req.setAttribute("pageTitle", "Seller Dashboard - JayasriMart");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/seller/seller-dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error loading seller dashboard for user {}: {}",
                    currentUser.getId(), e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load seller dashboard metrics.");
            req.getRequestDispatcher("/WEB-INF/views/seller/seller-dashboard.jsp").forward(req, resp);
        }
    }
}
