package com.jayasrimart.controller;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.filter.AuthFilter;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
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
import java.util.stream.Collectors;

/**
 * Controller managing platform user directory, role assignment, and account governance for administrators.
 */
@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users", "/admin/users/role"})
public class AdminUserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserServlet.class);

    private final AdminService adminService;

    /**
     * Default constructor.
     */
    public AdminUserServlet() {
        this(new AdminServiceImpl());
    }

    /**
     * Parameterized constructor for dependency injection.
     *
     * @param adminService the admin service
     */
    public AdminUserServlet(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        try {
            List<User> users = adminService.getAllUsers();
            String roleFilter = req.getParameter("role");

            if (roleFilter != null && !roleFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
                Role role = Role.fromString(roleFilter.trim());
                if (role != null) {
                    users = users.stream()
                            .filter(u -> u.getRole() == role)
                            .collect(Collectors.toList());
                }
            }

            req.setAttribute("users", users);
            req.setAttribute("selectedRole", roleFilter);
            req.setAttribute("pageTitle", "User Management - JayasriMart Admin");
            CsrfUtil.getToken(session);

            req.getRequestDispatcher("/WEB-INF/views/admin/admin-users.jsp").forward(req, resp);
        } catch (Exception e) {
            LOGGER.error("Error displaying admin user list: {}", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load user list.");
            req.getRequestDispatcher("/WEB-INF/views/admin/admin-users.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        UserResponseDTO currentUser = (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY);

        if (!CsrfUtil.isValid(req)) {
            LOGGER.warn("CSRF token validation failed during user role update.");
            resp.sendRedirect(req.getContextPath() + "/admin/users?error=Security+validation+failed.+Please+try+again.");
            return;
        }

        try {
            String userIdStr = req.getParameter("userId");
            String roleStr = req.getParameter("role");

            if (userIdStr == null || roleStr == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/users?error=User+ID+and+role+are+required.");
                return;
            }

            Long targetUserId = Long.parseLong(userIdStr.trim());
            Role targetRole = Role.fromString(roleStr.trim());

            User updatedUser = adminService.updateUserRole(currentUser.getId(), targetUserId, targetRole);

            resp.sendRedirect(req.getContextPath() + "/admin/users?success="
                    + URLEncoder.encode("User " + updatedUser.getEmail() + " role updated to " + targetRole.name() + ".",
                    StandardCharsets.UTF_8));
        } catch (AppException e) {
            LOGGER.warn("User role modification failed: {}", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/users?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Unexpected error in AdminUserServlet doPost: {}", e.getMessage(), e);
            resp.sendRedirect(req.getContextPath() + "/admin/users?error="
                    + URLEncoder.encode("Failed to update user role.", StandardCharsets.UTF_8));
        }
    }
}
