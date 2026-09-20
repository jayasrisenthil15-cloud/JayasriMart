package com.jayasrimart.service;

import com.jayasrimart.dto.UserLoginRequest;
import com.jayasrimart.dto.UserRegisterRequest;
import com.jayasrimart.dto.UserResponseDTO;
import com.jayasrimart.exception.AuthenticationException;
import com.jayasrimart.exception.DuplicateResourceException;
import com.jayasrimart.exception.ValidationException;

/**
 * Service interface governing authentication, user registration, and profile management.
 */
public interface AuthService {

    /**
     * Authenticates a user given email and plain-text password.
     *
     * @param request the login credentials
     * @return the authenticated user's details as a {@link UserResponseDTO}
     * @throws ValidationException if the request fields fail validation
     * @throws AuthenticationException if credentials do not match
     */
    UserResponseDTO login(UserLoginRequest request);

    /**
     * Registers a new customer (BUYER) or merchant (SELLER).
     * Registration of the ADMIN role via this method is strictly rejected.
     *
     * @param request the registration details
     * @return the newly registered user as a {@link UserResponseDTO}
     * @throws ValidationException if the input is invalid or passwords do not match
     * @throws DuplicateResourceException if an account with the specified email already exists
     */
    UserResponseDTO register(UserRegisterRequest request);

    /**
     * Retrieves the profile information for a given user ID.
     *
     * @param userId the user ID
     * @return user details as a {@link UserResponseDTO}
     * @throws ValidationException if user ID is null
     */
    UserResponseDTO getUserProfile(Long userId);

    /**
     * Updates profile name for an existing user.
     *
     * @param userId the user ID
     * @param name the new display name
     * @return updated user details as a {@link UserResponseDTO}
     * @throws ValidationException if inputs are invalid
     */
    UserResponseDTO updateProfile(Long userId, String name);
}
