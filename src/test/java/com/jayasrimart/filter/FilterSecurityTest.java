package com.jayasrimart.filter;

import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthFilter} and {@link RoleFilter}.
 */
@ExtendWith(MockitoExtension.class)
class FilterSecurityTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    private AuthFilter authFilter;
    private RoleFilter roleFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter();
        roleFilter = new RoleFilter();
    }

    @Test
    void testAuthFilter_Unauthenticated_RedirectsToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/cart");
        when(request.getQueryString()).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        authFilter.doFilter(request, response, chain);

        verify(response).sendRedirect(contains("/login?info="));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testAuthFilter_Authenticated_PassesThrough() throws Exception {
        UserResponseDTO user = UserResponseDTO.builder()
                .id(1L)
                .email("buyer@test.com")
                .name("Buyer User")
                .role(Role.BUYER)
                .build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER_KEY)).thenReturn(user);

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testRoleFilter_BuyerAccessingAdmin_Forbidden() throws Exception {
        UserResponseDTO buyer = UserResponseDTO.builder()
                .id(1L)
                .email("buyer@test.com")
                .name("Buyer")
                .role(Role.BUYER)
                .build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER_KEY)).thenReturn(buyer);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/admin/users");

        roleFilter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains("Access denied"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testRoleFilter_SellerAccessingSeller_PassesThrough() throws Exception {
        UserResponseDTO seller = UserResponseDTO.builder()
                .id(2L)
                .email("seller@test.com")
                .name("Seller")
                .role(Role.SELLER)
                .build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER_KEY)).thenReturn(seller);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/seller/products");

        roleFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    void testRoleFilter_AdminAccessingAll_PassesThrough() throws Exception {
        UserResponseDTO admin = UserResponseDTO.builder()
                .id(3L)
                .email("admin@test.com")
                .name("Admin")
                .role(Role.ADMIN)
                .build();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(AuthFilter.SESSION_USER_KEY)).thenReturn(admin);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/admin/dashboard");

        roleFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
