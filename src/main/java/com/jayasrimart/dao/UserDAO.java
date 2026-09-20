package com.jayasrimart.dao;

import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface defining database operations for {@link User} entities.
 */
public interface UserDAO {

    /**
     * Locates a user by their unique primary key.
     *
     * @param id the user ID
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findById(Long id);

    /**
     * Locates a user by their unique email address.
     *
     * @param email the email address
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether an account exists with the specified email address.
     *
     * @param email the email address
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Inserts a new user record into the database.
     *
     * @param user the user entity to persist
     * @return the created user with populated generated ID and timestamp
     */
    User create(User user);

    /**
     * Updates an existing user record.
     *
     * @param user the user entity containing updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean update(User user);

    /**
     * Retrieves all users registered in the system.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Retrieves all users filtered by their role.
     *
     * @param role the user role
     * @return list of matching users
     */
    List<User> findByRole(Role role);

    /**
     * Counts the total number of users with a specific role.
     *
     * @param role the user role
     * @return total user count
     */
    int countByRole(Role role);

    /**
     * Counts the total number of users across all roles.
     *
     * @return total user count
     */
    int countAll();

    /**
     * Updates the role of a specified user.
     *
     * @param userId the user ID
     * @param role the new role
     * @return true if updated, false otherwise
     */
    boolean updateRole(Long userId, Role role);
}
