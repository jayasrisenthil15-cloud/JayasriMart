package com.jayasrimart.service;

import com.jayasrimart.dao.UserDAO;
import com.jayasrimart.dto.UserLoginRequest;
import com.jayasrimart.dto.UserRegisterRequest;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AuthenticationException;
import com.jayasrimart.exception.DuplicateResourceException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.service.impl.AuthServiceImpl;
import com.jayasrimart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userDAO);
    }

    @Test
    @DisplayName("Successful login returns UserResponseDTO without password hash")
    void testLoginSuccess() {
        String email = "buyer@test.com";
        String plainPassword = "SecretPassword123";
        String hash = PasswordUtil.hash(plainPassword);

        User mockUser = new User(10L, "Test Buyer", email, hash, Role.BUYER, new Timestamp(System.currentTimeMillis()));
        when(userDAO.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserLoginRequest request = new UserLoginRequest(email, plainPassword);
        UserResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Buyer", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(Role.BUYER, response.getRole());
    }

    @Test
    @DisplayName("Login with invalid password throws AuthenticationException")
    void testLoginInvalidPassword() {
        String email = "buyer@test.com";
        String hash = PasswordUtil.hash("CorrectPassword");

        User mockUser = new User(10L, "Test Buyer", email, hash, Role.BUYER, new Timestamp(System.currentTimeMillis()));
        when(userDAO.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserLoginRequest request = new UserLoginRequest(email, "WrongPassword");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login with non-existent email throws AuthenticationException")
    void testLoginUserNotFound() {
        when(userDAO.findByEmail(anyString())).thenReturn(Optional.empty());

        UserLoginRequest request = new UserLoginRequest("unknown@test.com", "Password123");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Successful registration for Buyer creates user and returns DTO")
    void testRegisterBuyerSuccess() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Jane Doe", "jane@example.com", "SecurePass123", "SecurePass123", Role.BUYER
        );

        when(userDAO.existsByEmail("jane@example.com")).thenReturn(false);
        when(userDAO.create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            u.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            return u;
        });

        UserResponseDTO response = authService.register(request);
        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Role.BUYER, response.getRole());

        verify(userDAO).create(any(User.class));
    }

    @Test
    @DisplayName("Registering with ADMIN role throws ValidationException")
    void testRegisterAdminRejected() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Admin Wannabe", "fakeadmin@test.com", "Pass1234", "Pass1234", Role.ADMIN
        );

        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Registration with duplicate email throws DuplicateResourceException")
    void testRegisterDuplicateEmail() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Jane Doe", "existing@example.com", "SecurePass123", "SecurePass123", Role.BUYER
        );

        when(userDAO.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Registration with password mismatch throws ValidationException")
    void testRegisterPasswordMismatch() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Jane Doe", "jane@example.com", "Password1", "Password2", Role.BUYER
        );

        assertThrows(ValidationException.class, () -> authService.register(request));
    }
}
