package com.jayasrimart.controller;

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

import java.io.IOException;

/**
 * Controller for viewing and editing the user account profile.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AuthService authService;

    public ProfileServlet() {
        this(new AuthServiceImpl());
    }

    public ProfileServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO sessionUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        try {
            UserResponseDTO profile = authService.getUserProfile(sessionUser.getId());
            req.setAttribute("user", profile);
            CsrfUtil.getToken(session);
            req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
        } catch (AppException e) {
            resp.sendRedirect(req.getContextPath() + "/?error=" + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(req)) {
            req.setAttribute("errorMessage", "Security token expired. Please retry.");
            doGet(req, resp);
            return;
        }

        HttpSession session = req.getSession(false);
        UserResponseDTO sessionUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);
        String newName = req.getParameter("name");

        try {
            UserResponseDTO updated = authService.updateProfile(sessionUser.getId(), newName);
            session.setAttribute(AuthFilter.SESSION_USER_KEY, updated);
            req.setAttribute("user", updated);
            req.setAttribute("successMessage", "Profile updated successfully!");
        } catch (AppException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("user", sessionUser);
        }

        CsrfUtil.getToken(session);
        req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
    }
}
