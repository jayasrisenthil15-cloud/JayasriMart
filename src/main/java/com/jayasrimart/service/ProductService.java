package com.jayasrimart.service;

import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.model.Product;
import com.jayasrimart.model.Review;

import java.util.List;

/**
 * Service interface encapsulating business logic, validation, and catalog queries for {@link Product} entities.
 */
public interface ProductService {

    /**
     * Retrieves featured top-rated products for the landing page or buyer showcase.
     *
     * @param limit maximum products to fetch
     * @return list of featured products
     */
    List<Product> getFeaturedProducts(int limit);

    /**
     * Searches and filters products according to dynamic multi-attribute criteria.
     *
     * @param criteria the filter, search, sort, and pagination criteria
     * @return list of matching products
     */
    List<Product> searchProducts(ProductSearchCriteria criteria);

    /**
     * Counts the total number of products matching the filter criteria.
     *
     * @param criteria the filter criteria
     * @return total matching count
     */
    int countProducts(ProductSearchCriteria criteria);

    /**
     * Retrieves full details for a product by its ID, including seller attribution.
     *
     * @param id the product ID
     * @return the populated product entity
     * @throws com.jayasrimart.exception.ResourceNotFoundException if product not found
     */
    Product getProductDetails(Long id);

    /**
     * Retrieves all distinct categories available across active products.
     *
     * @return list of category names
     */
    List<String> getAllCategories();

    /**
     * Retrieves all customer reviews associated with a product.
     *
     * @param productId the product ID
     * @return list of reviews
     */
    List<Review> getProductReviews(Long productId);

    /**
     * Retrieves all products belonging to a specific seller for inventory management.
     *
     * @param sellerId the seller's user ID
     * @return list of seller's products
     */
    List<Product> getSellerProducts(Long sellerId);

    /**
     * Creates a new product on behalf of an authenticated seller.
     *
     * @param sellerId authenticated seller ID
     * @param product product entity
     * @return persisted product
     */
    Product createProduct(Long sellerId, Product product);

    /**
     * Updates an existing product verifying seller ownership.
     *
     * @param sellerId authenticated seller ID
     * @param product updated product entity
     * @return updated product
     */
    Product updateProduct(Long sellerId, Product product);

    /**
     * Deletes a product verifying seller ownership.
     *
     * @param sellerId authenticated seller ID
     * @param productId product ID
     */
    void deleteProduct(Long sellerId, Long productId);
}
