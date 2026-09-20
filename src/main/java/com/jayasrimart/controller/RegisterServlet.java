package com.jayasrimart.controller;

import com.jayasrimart.dto.UserRegisterRequest;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Role;
import com.jayasrimart.service.AuthService;
import com.jayasrimart.service.impl.AuthServiceImpl;
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
 * Controller handling user registration for BUYER and SELLER accounts.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServlet.class);

    private final AuthService authService;

    public RegisterServlet() {
        this(new AuthServiceImpl());
    }

    public RegisterServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER_KEY) != null) {
            resp.sendRedirect(req.getContextPath() + "/products");
            return;
        }

        CsrfUtil.getToken(req.getSession(true));
        req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1. Verify CSRF token
        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token verification failed during registration attempt.");
            req.setAttribute("errorMessage", "Security validation failed. Please refresh and try again.");
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
            return;
        }

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String roleParam = req.getParameter("role");

        Role role = Role.fromString(roleParam);

        UserRegisterRequest registerRequest = new UserRegisterRequest(name, email, password, confirmPassword, role);

        try {
            UserResponseDTO userDTO = authService.register(registerRequest);

            // Auto-login registered user and establish fresh session
            req.changeSessionId();
            HttpSession session = req.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER_KEY, userDTO);

            if (Role.SELLER.equals(userDTO.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/seller/dashboard?registered=true");
            } else {
                resp.sendRedirect(req.getContextPath() + "/products?registered=true");
            }

        } catch (AppException e) {
            LOGGER.warn("Registration rejected for '{}': {}", email, e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("selectedRole", roleParam);
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }
}
