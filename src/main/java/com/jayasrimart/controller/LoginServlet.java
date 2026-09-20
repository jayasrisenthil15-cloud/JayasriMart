package com.jayasrimart.controller;

import com.jayasrimart.dto.UserLoginRequest;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
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
 * Controller managing user authentication and session establishment.
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class);

    private final AuthService authService;

    public LoginServlet() {
        this(new AuthServiceImpl());
    }

    public LoginServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.SESSION_USER_KEY) != null) {
            redirectRoleHome(req, resp, (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY));
            return;
        }

        // Ensure CSRF token is available
        CsrfUtil.getToken(req.getSession(true));
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1. Verify CSRF token
        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token verification failed during login attempt.");
            req.setAttribute("errorMessage", "Security validation failed. Please refresh and try again.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
            return;
        }

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        try {
            UserResponseDTO userDTO = authService.login(loginRequest);

            // 2. Prevent Session Fixation: Regenerate session ID upon authentication
            req.changeSessionId();
            HttpSession session = req.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER_KEY, userDTO);

            // 3. Handle post-login redirection if user was originally trying to access a protected URL
            String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                session.removeAttribute("redirectAfterLogin");
                resp.sendRedirect(redirectUrl);
                return;
            }

            // 4. Role-based landing redirection
            redirectRoleHome(req, resp, userDTO);

        } catch (AppException e) {
            LOGGER.warn("Login failed for user '{}': {}", email, e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void redirectRoleHome(HttpServletRequest req, HttpServletResponse resp, UserResponseDTO user)
            throws IOException {
        String contextPath = req.getContextPath();
        switch (user.getRole()) {
            case SELLER -> resp.sendRedirect(contextPath + "/seller/dashboard");
            case ADMIN -> resp.sendRedirect(contextPath + "/admin/dashboard");
            default -> resp.sendRedirect(contextPath + "/products");
        }
    }
}
