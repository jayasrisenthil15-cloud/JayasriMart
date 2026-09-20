package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.WishlistDAO;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link WishlistService}.
 * Handles wishlist toggling, validation, and content-based recommendation synthesis.
 */
public class WishlistServiceImpl implements WishlistService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WishlistServiceImpl.class);

    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public WishlistServiceImpl() {
        this(DaoFactory.getWishlistDAO(), DaoFactory.getProductDAO());
    }

    /**
     * Parameterized constructor for dependency injection and testing.
     *
     * @param wishlistDAO the wishlist DAO
     * @param productDAO the product DAO
     */
    public WishlistServiceImpl(WishlistDAO wishlistDAO, ProductDAO productDAO) {
        if (wishlistDAO == null) {
            throw new IllegalArgumentException("WishlistDAO cannot be null");
        }
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO cannot be null");
        }
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
    }

    @Override
    public boolean toggleWishlist(Long userId, Long productId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (wishlistDAO.exists(userId, productId)) {
            wishlistDAO.remove(userId, productId);
            LOGGER.info("User {} removed product {} from wishlist", userId, productId);
            return false;
        } else {
            wishlistDAO.add(userId, productId);
            LOGGER.info("User {} added product {} to wishlist", userId, productId);
            return true;
        }
    }

    @Override
    public boolean removeFromWishlist(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return wishlistDAO.remove(userId, productId);
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        return wishlistDAO.exists(userId, productId);
    }

    @Override
    public List<Product> getWishlist(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }
        return wishlistDAO.findWishlistProducts(userId);
    }

    @Override
    public List<Long> getWishlistProductIds(Long userId) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }
        return wishlistDAO.findProductIdsByUserId(userId);
    }

    @Override
    public int getWishlistCount(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return wishlistDAO.countByUserId(userId);
    }

    @Override
    public List<Product> getRecommendations(Long userId, int limit) {
        int targetLimit = limit > 0 ? limit : 4;
        List<Product> recommendations = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();

        if (userId != null && userId > 0) {
            List<Long> wishlistedIds = wishlistDAO.findProductIdsByUserId(userId);
            seenIds.addAll(wishlistedIds);

            List<String> categories = wishlistDAO.findInteractedCategories(userId);
            if (!categories.isEmpty()) {
                int perCategoryLimit = Math.max(2, targetLimit / categories.size() + 1);
                for (String category : categories) {
                    List<Product> categoryProducts = productDAO.findByCategory(category, perCategoryLimit);
                    for (Product p : categoryProducts) {
                        if (!seenIds.contains(p.getId()) && recommendations.size() < targetLimit) {
                            recommendations.add(p);
                            seenIds.add(p.getId());
                        }
                    }
                }
            }
        }

        // Fill remaining slots with featured top-rated products
        if (recommendations.size() < targetLimit) {
            List<Product> featured = productDAO.findFeatured(targetLimit * 2);
            for (Product p : featured) {
                if (!seenIds.contains(p.getId()) && recommendations.size() < targetLimit) {
                    recommendations.add(p);
                    seenIds.add(p.getId());
                }
            }
        }

        return recommendations;
    }
}
