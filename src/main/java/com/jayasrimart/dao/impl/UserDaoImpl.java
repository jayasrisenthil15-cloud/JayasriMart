package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.UserDAO;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Role;
import com.jayasrimart.model.User;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of the {@link UserDAO} interface.
 * Uses strictly parameterized {@link PreparedStatement} and try-with-resources.
 */
public class UserDaoImpl implements UserDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

    private static final String SQL_FIND_BY_ID =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE id = ?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String SQL_EXISTS_BY_EMAIL =
            "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String SQL_INSERT =
            "INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE users SET name = ?, email = ?, password_hash = ?, role = ? WHERE id = ?";

    private static final String SQL_FIND_ALL =
            "SELECT id, name, email, password_hash, role, created_at FROM users ORDER BY id ASC";

    private static final String SQL_FIND_BY_ROLE =
            "SELECT id, name, email, password_hash, role, created_at FROM users WHERE role = ? ORDER BY id ASC";

    private static final String SQL_COUNT_BY_ROLE =
            "SELECT COUNT(*) AS total FROM users WHERE role = ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) AS total FROM users";

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findById for userId {}: {}", id, e.getMessage(), e);
            throw new AppException("Failed to query user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findByEmail for {}: {}", email, e.getMessage(), e);
            throw new AppException("Failed to query user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_EMAIL)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in existsByEmail for {}: {}", email, e.getMessage(), e);
            throw new AppException("Failed to check email existence", e);
        }
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User entity to create cannot be null");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setTimestamp(5, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AppException("Failed to create user: no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    user.setCreatedAt(now);
                    return user;
                } else {
                    throw new AppException("Failed to create user: generated ID not retrieved.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error creating user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new AppException("Failed to persist user in database", e);
        }
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user ID cannot be null for update");
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setLong(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating user {}: {}", user.getId(), e.getMessage(), e);
            throw new AppException("Failed to update user", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findAll users: {}", e.getMessage(), e);
            throw new AppException("Failed to retrieve users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        if (role == null) {
            return list;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ROLE)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findByRole users {}: {}", role, e.getMessage(), e);
            throw new AppException("Failed to retrieve users by role", e);
        }
        return list;
    }

    @Override
    public int countByRole(Role role) {
        if (role == null) {
            return 0;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_ROLE)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in countByRole {}: {}", role, e.getMessage(), e);
            throw new AppException("Failed to count users by role", e);
        }
        return 0;
    }

    @Override
    public int countAll() {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in countAll users: {}", e.getMessage(), e);
            throw new AppException("Failed to count all users", e);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
