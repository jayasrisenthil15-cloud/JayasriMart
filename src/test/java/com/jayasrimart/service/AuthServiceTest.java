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
    @DisplayName("Login succeeds with valid credentials")
    void testLoginSuccess() {
        String email = "buyer1@jayasrimart.com";
        String rawPassword = "Buyer@123";
        String passwordHash = PasswordUtil.hash(rawPassword);

        User mockUser = new User();
        mockUser.setId(10L);
        mockUser.setName("Test Buyer");
        mockUser.setEmail(email);
        mockUser.setPasswordHash(passwordHash);
        mockUser.setRole(Role.BUYER);
        mockUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        when(userDAO.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserLoginRequest request = new UserLoginRequest(email, rawPassword);
        UserResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Buyer", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(Role.BUYER, response.getRole());
    }

    @Test
    @DisplayName("Login fails with invalid password")
    void testLoginWrongPassword() {
        String email = "buyer1@jayasrimart.com";
        String correctPassword = "Buyer@123";
        String passwordHash = PasswordUtil.hash(correctPassword);

        User mockUser = new User();
        mockUser.setId(10L);
        mockUser.setName("Test Buyer");
        mockUser.setEmail(email);
        mockUser.setPasswordHash(passwordHash);
        mockUser.setRole(Role.BUYER);

        when(userDAO.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserLoginRequest request = new UserLoginRequest(email, "WrongPassword@999");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Login fails when user email does not exist")
    void testLoginUserNotFound() {
        String email = "nonexistent@jayasrimart.com";
        when(userDAO.findByEmail(email)).thenReturn(Optional.empty());

        UserLoginRequest request = new UserLoginRequest(email, "Password@123");
        assertThrows(AuthenticationException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Registration succeeds for valid buyer request")
    void testRegisterSuccess() {
        String email = "newuser@example.com";
        UserRegisterRequest request = new UserRegisterRequest(
                "New User",
                email,
                "Password@123",
                "Password@123",
                Role.BUYER
        );

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(101L);
            u.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            return u;
        });

        UserResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("New User", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(Role.BUYER, response.getRole());
        verify(userDAO).create(any(User.class));
    }

    @Test
    @DisplayName("Registration rejects duplicate email addresses")
    void testRegisterDuplicateEmail() {
        String email = "existing@example.com";
        UserRegisterRequest request = new UserRegisterRequest(
                "Existing User",
                email,
                "Password@123",
                "Password@123",
                Role.BUYER
        );

        when(userDAO.existsByEmail(email)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Registration prohibits public registration with ADMIN role")
    void testRegisterProhibitsAdminRole() {
        UserRegisterRequest request = new UserRegisterRequest(
                "Hacker Admin",
                "hacker@example.com",
                "Password@123",
                "Password@123",
                Role.ADMIN
        );

        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any(User.class));
    }

    @Test
    @DisplayName("Registration rejects mismatched password confirmation")
    void testRegisterPasswordMismatch() {
        UserRegisterRequest request = new UserRegisterRequest(
                "User Name",
                "user@example.com",
                "Password@123",
                "DifferentPassword@123",
                Role.BUYER
        );

        assertThrows(ValidationException.class, () -> authService.register(request));
        verify(userDAO, never()).create(any(User.class));
    }
}
