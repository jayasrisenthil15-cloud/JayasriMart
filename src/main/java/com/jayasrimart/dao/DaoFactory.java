package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.UserDaoImpl;

/**
 * Factory class for instantiating DAO implementations.
 * Follows the Factory design pattern, decoupling service implementations from concrete JDBC DAOs.
 */
public final class DaoFactory {

    private static final UserDAO USER_DAO = new UserDaoImpl();

    private DaoFactory() {
        // Prevent instantiation
    }

    /**
     * Obtains the singleton {@link UserDAO} implementation instance.
     *
     * @return the UserDAO instance
     */
    public static UserDAO getUserDAO() {
        return USER_DAO;
    }
}
