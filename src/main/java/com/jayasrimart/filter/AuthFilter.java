package com.jayasrimart.filter;

import com.jayasrimart.dto.UserResponseDTO;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Filter ensuring that only authenticated users can access protected application endpoints.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/profile/*",
        "/cart/*",
        "/checkout/*",
        "/orders/*",
        "/reviews/*",
        "/reviews",
        "/buyer/*",
        "/seller/*",
        "/admin/*"
})
public class AuthFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);
    public static final String SESSION_USER_KEY = "currentUser";

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
                    ? (UserResponseDTO) session.getAttribute(SESSION_USER_KEY)
                    : null;

            if (currentUser == null) {
                String requestUri = httpRequest.getRequestURI();
                String queryString = httpRequest.getQueryString();
                String fullTarget = (queryString != null) ? requestUri + "?" + queryString : requestUri;

                LOGGER.debug("Unauthenticated access attempt to '{}'. Redirecting to login.", fullTarget);

                if (session != null) {
                    session.setAttribute("redirectAfterLogin", fullTarget);
                }

                String loginUrl = httpRequest.getContextPath() + "/login?info="
                        + URLEncoder.encode("Please sign in to access this page.", StandardCharsets.UTF_8);
                httpResponse.sendRedirect(loginUrl);
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
