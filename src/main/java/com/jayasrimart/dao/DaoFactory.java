package com.jayasrimart.dao;

import com.jayasrimart.dao.impl.CartDaoImpl;
import com.jayasrimart.dao.impl.OrderDaoImpl;
import com.jayasrimart.dao.impl.ProductDaoImpl;
import com.jayasrimart.dao.impl.ReviewDaoImpl;
import com.jayasrimart.dao.impl.UserDaoImpl;

/**
 * Factory class for instantiating DAO implementations.
 * Follows the Factory design pattern, decoupling service implementations from concrete JDBC DAOs.
 */
public final class DaoFactory {

    private static final UserDAO USER_DAO = new UserDaoImpl();
    private static final ProductDAO PRODUCT_DAO = new ProductDaoImpl();
    private static final ReviewDAO REVIEW_DAO = new ReviewDaoImpl();
    private static final CartDAO CART_DAO = new CartDaoImpl();
    private static final OrderDAO ORDER_DAO = new OrderDaoImpl();

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

    /**
     * Obtains the singleton {@link ProductDAO} implementation instance.
     *
     * @return the ProductDAO instance
     */
    public static ProductDAO getProductDAO() {
        return PRODUCT_DAO;
    }

    /**
     * Obtains the singleton {@link ReviewDAO} implementation instance.
     *
     * @return the ReviewDAO instance
     */
    public static ReviewDAO getReviewDAO() {
        return REVIEW_DAO;
    }

    /**
     * Obtains the singleton {@link CartDAO} implementation instance.
     *
     * @return the CartDAO instance
     */
    public static CartDAO getCartDAO() {
        return CART_DAO;
    }

    /**
     * Obtains the singleton {@link OrderDAO} implementation instance.
     *
     * @return the OrderDAO instance
     */
    public static OrderDAO getOrderDAO() {
        return ORDER_DAO;
    }
}
