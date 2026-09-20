package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.UserDAO;
import com.jayasrimart.dto.UserLoginRequest;
import com.jayasrimart.dto.UserRegisterRequest;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AuthenticationException;
import com.jayasrimart.exception.DuplicateResourceException;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.service.AuthService;
import com.jayasrimart.util.PasswordUtil;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Implementation of the {@link AuthService} interface.
 * Implements business rules, input validations, and depends strictly on the {@link UserDAO} interface.
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDAO userDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public AuthServiceImpl() {
        this(DaoFactory.getUserDAO());
    }

    /**
     * Parameterized constructor for dependency injection and unit testing.
     *
     * @param userDAO the user DAO interface
     */
    public AuthServiceImpl(UserDAO userDAO) {
        if (userDAO == null) {
            throw new IllegalArgumentException("UserDAO dependency cannot be null");
        }
        this.userDAO = userDAO;
    }

    @Override
    public UserResponseDTO login(UserLoginRequest request) {
        // 1. Validate inputs at top of service method
        if (request == null) {
            throw new ValidationException("Invalid login request payload.");
        }
        String email = ValidationUtil.validateEmail(request.getEmail());
        String password = ValidationUtil.requireNonBlank(request.getPassword(), "password");

        LOGGER.debug("Attempting authentication for email: {}", email);

        // 2. Fetch user by email
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            LOGGER.warn("Authentication failed: user not found for email: {}", email);
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userOpt.get();

        // 3. Verify password hash using jBCrypt (never log passwords!)
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            LOGGER.warn("Authentication failed: password mismatch for user ID: {}", user.getId());
            throw new AuthenticationException("Invalid email or password.");
        }

        LOGGER.info("User ID {} ({}) successfully authenticated with role {}", user.getId(), user.getEmail(), user.getRole());
        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO register(UserRegisterRequest request) {
        // 1. Server-side validation at top of service method
        if (request == null) {
            throw new ValidationException("Invalid registration request payload.");
        }

        String name = ValidationUtil.requireNonBlank(request.getName(), "name");
        if (name.length() < 2 || name.length() > 100) {
            throw new ValidationException("name", "Name must be between 2 and 100 characters.");
        }

        String email = ValidationUtil.validateEmail(request.getEmail());
        String password = ValidationUtil.validatePassword(request.getPassword());
        String confirmPassword = ValidationUtil.requireNonBlank(request.getConfirmPassword(), "confirmPassword");

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("confirmPassword", "Passwords do not match.");
        }

        Role role = request.getRole();
        if (role == null) {
            throw new ValidationException("role", "Please select an account role (Buyer or Seller).");
        }

        // Enforce security rule: No public admin registration
        if (Role.ADMIN.equals(role)) {
            LOGGER.warn("Security alert: Attempted public registration with ADMIN role for email {}", email);
            throw new ValidationException("role", "Public administrator registration is prohibited.");
        }

        // 2. Check email uniqueness
        if (userDAO.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email '" + email + "' is already registered.");
        }

        // 3. Hash password using jBCrypt
        String passwordHash = PasswordUtil.hash(password);

        // 4. Create and persist user entity
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordHash);
        newUser.setRole(role);

        User createdUser = userDAO.create(newUser);
        LOGGER.info("Successfully registered new user ID {} ({}) with role {}", createdUser.getId(), createdUser.getEmail(), createdUser.getRole());

        return mapToDTO(createdUser);
    }

    @Override
    public UserResponseDTO getUserProfile(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToDTO(user);
    }

    @Override
    public UserResponseDTO updateProfile(Long userId, String name) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        String validName = ValidationUtil.requireNonBlank(name, "name");
        if (validName.length() < 2 || validName.length() > 100) {
            throw new ValidationException("name", "Name must be between 2 and 100 characters.");
        }

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setName(validName);
        userDAO.update(user);
        LOGGER.info("Updated profile name for user ID: {}", userId);

        return mapToDTO(user);
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
