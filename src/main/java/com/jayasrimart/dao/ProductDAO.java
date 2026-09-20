package com.jayasrimart.dao;

import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.model.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface defining database operations for {@link Product} entities.
 */
public interface ProductDAO {

    /**
     * Finds a single active or inactive product by its ID, joined with seller details.
     *
     * @param id the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(Long id);

    /**
     * Executes a combined search and filter query with pagination and sorting in a single database query.
     *
     * @param criteria the search, category, price, rating, and pagination filters
     * @return list of matching products
     */
    List<Product> searchAndFilter(ProductSearchCriteria criteria);

    /**
     * Counts the total number of products matching the given search and filter criteria.
     *
     * @param criteria the search criteria
     * @return total matching count
     */
    int countSearchAndFilter(ProductSearchCriteria criteria);

    /**
     * Retrieves featured top-rated active products for the landing and home pages.
     *
     * @param limit maximum number of products to return
     * @return list of featured products
     */
    List<Product> findFeatured(int limit);

    /**
     * Retrieves active products belonging to a specific category.
     *
     * @param category category name
     * @param limit maximum number to return
     * @return list of products
     */
    List<Product> findByCategory(String category, int limit);

    /**
     * Retrieves all products listed by a specific seller.
     *
     * @param sellerId the seller's user ID
     * @return list of seller's products
     */
    List<Product> findBySellerId(Long sellerId);

    /**
     * Retrieves all distinct product categories present in the database.
     *
     * @return list of distinct category names
     */
    List<String> findAllCategories();

    /**
     * Inserts a new product record into the database.
     *
     * @param product the product entity
     * @return the created product with populated ID and timestamp
     */
    Product create(Product product);

    /**
     * Updates an existing product entity.
     *
     * @param product the product entity with updated fields
     * @return true if updated successfully, false otherwise
     */
    boolean update(Product product);

    /**
     * Deletes a product by ID.
     *
     * @param id the product ID
     * @return true if deleted, false otherwise
     */
    boolean delete(Long id);

    /**
     * Updates the stock quantity of a product inside an existing transaction connection.
     *
     * @param productId the product ID
     * @param quantityDelta quantity to subtract (positive for checkout reduction, negative for restock)
     * @param conn the active transaction Connection
     * @return true if updated successfully and stock remained non-negative
     */
    boolean updateStock(Long productId, int quantityDelta, Connection conn);

    /**
     * Updates the average rating of a product inside an existing transaction connection.
     *
     * @param productId the product ID
     * @param newAvgRating the newly computed average rating
     * @param conn the active transaction Connection
     * @return true if updated successfully
     */
    boolean updateAvgRating(Long productId, BigDecimal newAvgRating, Connection conn);

    /**
     * Counts total number of products across all categories and statuses.
     *
     * @return total product count
     */
    int countAll();
}
