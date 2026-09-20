package com.jayasrimart.service.impl;

import com.jayasrimart.dao.DaoFactory;
import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dao.ReviewDAO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.ResourceNotFoundException;
import com.jayasrimart.exception.UnauthorizedException;
import com.jayasrimart.exception.ValidationException;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Review;
import com.jayasrimart.service.ProductService;
import com.jayasrimart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of the {@link ProductService} interface.
 * Enforces business logic and depends strictly on {@link ProductDAO} and {@link ReviewDAO} interfaces.
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDAO productDAO;
    private final ReviewDAO reviewDAO;

    /**
     * Default constructor utilizing {@link DaoFactory}.
     */
    public ProductServiceImpl() {
        this(DaoFactory.getProductDAO(), DaoFactory.getReviewDAO());
    }

    /**
     * Parameterized constructor for dependency injection and unit tests.
     *
     * @param productDAO the product DAO interface
     * @param reviewDAO the review DAO interface
     */
    public ProductServiceImpl(ProductDAO productDAO, ReviewDAO reviewDAO) {
        if (productDAO == null) {
            throw new IllegalArgumentException("ProductDAO dependency cannot be null");
        }
        if (reviewDAO == null) {
            throw new IllegalArgumentException("ReviewDAO dependency cannot be null");
        }
        this.productDAO = productDAO;
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        int effectiveLimit = limit > 0 ? limit : 8;
        return productDAO.findFeatured(effectiveLimit);
    }

    @Override
    public List<Product> searchProducts(ProductSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new ProductSearchCriteria();
        }
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new ValidationException("price", "Minimum price cannot be greater than maximum price.");
        }
        return productDAO.searchAndFilter(criteria);
    }

    @Override
    public int countProducts(ProductSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new ProductSearchCriteria();
        }
        return productDAO.countSearchAndFilter(criteria);
    }

    @Override
    public Product getProductDetails(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id", "Invalid product ID.");
        }
        return productDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Override
    public List<String> getAllCategories() {
        return productDAO.findAllCategories();
    }

    @Override
    public List<Review> getProductReviews(Long productId) {
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }
        return reviewDAO.findByProductId(productId);
    }

    @Override
    public List<Product> getSellerProducts(Long sellerId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        return productDAO.findBySellerId(sellerId);
    }

    @Override
    public Product createProduct(Long sellerId, Product product) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        validateProduct(product);

        product.setSellerId(sellerId);
        product.setAvgRating(BigDecimal.ZERO);
        product.setActive(true);

        Product created = productDAO.create(product);
        LOGGER.info("Created product ID {} ('{}') for seller ID {}", created.getId(), created.getName(), sellerId);
        return created;
    }

    @Override
    public Product updateProduct(Long sellerId, Product product) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (product == null || product.getId() == null) {
            throw new ValidationException("product", "Product and product ID are required for update.");
        }
        validateProduct(product);

        Product existing = productDAO.findById(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + product.getId()));

        if (!sellerId.equals(existing.getSellerId())) {
            LOGGER.warn("Unauthorized product update attempt: Seller {} tried updating Product {} owned by Seller {}",
                    sellerId, product.getId(), existing.getSellerId());
            throw new UnauthorizedException("You are not authorized to update this product.");
        }

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStockQty(product.getStockQty());
        existing.setCategory(product.getCategory());
        existing.setImageUrl(product.getImageUrl());
        existing.setActive(product.isActive());

        productDAO.update(existing);
        LOGGER.info("Updated product ID {} for seller ID {}", existing.getId(), sellerId);
        return existing;
    }

    @Override
    public void deleteProduct(Long sellerId, Long productId) {
        if (sellerId == null || sellerId <= 0) {
            throw new ValidationException("sellerId", "Invalid seller ID.");
        }
        if (productId == null || productId <= 0) {
            throw new ValidationException("productId", "Invalid product ID.");
        }

        Product existing = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!sellerId.equals(existing.getSellerId())) {
            LOGGER.warn("Unauthorized product delete attempt: Seller {} tried deleting Product {} owned by Seller {}",
                    sellerId, productId, existing.getSellerId());
            throw new UnauthorizedException("You are not authorized to delete this product.");
        }

        productDAO.delete(productId);
        LOGGER.info("Deleted product ID {} by seller ID {}", productId, sellerId);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("product", "Product payload cannot be null.");
        }
        String name = ValidationUtil.requireNonBlank(product.getName(), "name");
        if (name.length() < 2 || name.length() > 200) {
            throw new ValidationException("name", "Product name must be between 2 and 200 characters.");
        }
        ValidationUtil.requireNonBlank(product.getCategory(), "category");
        ValidationUtil.validatePositiveAmount(product.getPrice(), "price");

        if (product.getStockQty() < 0) {
            throw new ValidationException("stockQty", "Stock quantity cannot be negative.");
        }
    }
}
