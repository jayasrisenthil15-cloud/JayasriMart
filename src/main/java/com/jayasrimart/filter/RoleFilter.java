package com.jayasrimart.filter;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.model.Role;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Filter enforcing Role-Based Access Control (RBAC) across distinct URL segments:
 * <ul>
 *   <li>{@code /seller/*} -> Restricted to SELLER (or ADMIN)</li>
 *   <li>{@code /admin/*} -> Restricted exclusively to ADMIN</li>
 *   <li>{@code /buyer/*} -> Restricted to BUYER (or ADMIN)</li>
 * </ul>
 */
@WebFilter(filterName = "RoleFilter", urlPatterns = {
        "/buyer/*",
        "/seller/*",
        "/admin/*"
})
public class RoleFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            HttpSession session = httpRequest.getSession(false);
            UserResponseDTO currentUser = (session != null)
                    ? (UserResponseDTO) session.getAttribute(AuthFilter.SESSION_USER_KEY)
                    : null;

            if (currentUser == null) {
                // Let AuthFilter handle unauthenticated requests
                chain.doFilter(request, response);
                return;
            }

            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            Role role = currentUser.getRole();

            boolean isAuthorized = true;

            if (path.startsWith("/admin")) {
                if (role != Role.ADMIN) {
                    isAuthorized = false;
                }
            } else if (path.startsWith("/seller")) {
                if (role != Role.SELLER && role != Role.ADMIN) {
                    isAuthorized = false;
                }
            } else if (path.startsWith("/buyer")) {
                if (role != Role.BUYER && role != Role.ADMIN) {
                    isAuthorized = false;
                }
            }

            if (!isAuthorized) {
                LOGGER.warn("Access denied: User ID {} ({}) with role {} attempted accessing restricted path '{}'",
                        currentUser.getId(), currentUser.getEmail(), role, path);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied for role " + role);
                return;
            }

            chain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
