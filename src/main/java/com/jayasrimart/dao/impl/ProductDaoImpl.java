package com.jayasrimart.dao.impl;

import com.jayasrimart.dao.ProductDAO;
import com.jayasrimart.dto.ProductSearchCriteria;
import com.jayasrimart.exception.AppException;
import com.jayasrimart.model.Product;
import com.jayasrimart.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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
 * JDBC implementation of the {@link ProductDAO} interface.
 * Implements combined search, category, price, and rating queries in ONE query.
 */
public class ProductDaoImpl implements ProductDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDaoImpl.class);

    private static final String BASE_SELECT =
            "SELECT p.id, p.seller_id, p.name, p.description, p.price, p.stock_qty, p.category, "
            + "p.image_url, p.avg_rating, p.active, p.created_at, u.name AS seller_name "
            + "FROM products p "
            + "JOIN users u ON p.seller_id = u.id ";

    private static final String SQL_FIND_BY_ID =
            BASE_SELECT + "WHERE p.id = ?";

    private static final String SQL_FIND_FEATURED =
            BASE_SELECT + "WHERE p.active = TRUE ORDER BY p.avg_rating DESC, p.created_at DESC LIMIT ?";

    private static final String SQL_FIND_BY_CATEGORY =
            BASE_SELECT + "WHERE p.active = TRUE AND LOWER(p.category) = LOWER(?) ORDER BY p.created_at DESC LIMIT ?";

    private static final String SQL_FIND_BY_SELLER =
            BASE_SELECT + "WHERE p.seller_id = ? ORDER BY p.created_at DESC";

    private static final String SQL_FIND_ALL_CATEGORIES =
            "SELECT DISTINCT category FROM products WHERE active = TRUE ORDER BY category ASC";

    private static final String SQL_INSERT =
            "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url, avg_rating, active, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE products SET name = ?, description = ?, price = ?, stock_qty = ?, category = ?, "
            + "image_url = ?, active = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM products WHERE id = ?";

    private static final String SQL_UPDATE_STOCK =
            "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";

    private static final String SQL_UPDATE_AVG_RATING =
            "UPDATE products SET avg_rating = ? WHERE id = ?";

    private static final String SQL_COUNT_ALL =
            "SELECT COUNT(*) AS total FROM products";

    @Override
    public Optional<Product> findById(Long id) {
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
            LOGGER.error("Database error in findById for productId {}: {}", id, e.getMessage(), e);
            throw new AppException("Failed to find product by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> searchAndFilter(ProductSearchCriteria criteria) {
        List<Product> products = new ArrayList<>();
        if (criteria == null) {
            criteria = new ProductSearchCriteria();
        }

        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildWhereConditions(criteria, sql, params);

        // Sorting
        String sortBy = criteria.getSortBy();
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price ASC, p.id ASC ");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.price DESC, p.id DESC ");
        } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY p.avg_rating DESC, p.id DESC ");
        } else {
            sql.append("ORDER BY p.created_at DESC, p.id DESC ");
        }

        // Pagination
        sql.append("LIMIT ? OFFSET ?");
        params.add(criteria.getLimit() > 0 ? criteria.getLimit() : 12);
        params.add(Math.max(criteria.getOffset(), 0));

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in searchAndFilter: {}", e.getMessage(), e);
            throw new AppException("Failed to execute product search query", e);
        }

        return products;
    }

    @Override
    public int countSearchAndFilter(ProductSearchCriteria criteria) {
        if (criteria == null) {
            criteria = new ProductSearchCriteria();
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM products p WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        buildWhereConditions(criteria, sql, params);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in countSearchAndFilter: {}", e.getMessage(), e);
            throw new AppException("Failed to count search results", e);
        }
        return 0;
    }

    private void buildWhereConditions(ProductSearchCriteria criteria, StringBuilder sql, List<Object> params) {
        if (Boolean.TRUE.equals(criteria.getActiveOnly())) {
            sql.append("AND p.active = TRUE ");
        }

        if (criteria.getSellerId() != null) {
            sql.append("AND p.seller_id = ? ");
            params.add(criteria.getSellerId());
        }

        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String kwParam = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
            params.add(kwParam);
            params.add(kwParam);
        }

        if (criteria.getCategory() != null && !criteria.getCategory().trim().isEmpty()
                && !"All".equalsIgnoreCase(criteria.getCategory())) {
            sql.append("AND LOWER(p.category) = LOWER(?) ");
            params.add(criteria.getCategory().trim());
        }

        if (criteria.getMinPrice() != null) {
            sql.append("AND p.price >= ? ");
            params.add(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            sql.append("AND p.price <= ? ");
            params.add(criteria.getMaxPrice());
        }

        if (criteria.getMinRating() != null && criteria.getMinRating().compareTo(BigDecimal.ZERO) > 0) {
            sql.append("AND p.avg_rating >= ? ");
            params.add(criteria.getMinRating());
        }
    }

    @Override
    public List<Product> findFeatured(int limit) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_FEATURED)) {
            ps.setInt(1, limit > 0 ? limit : 8);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findFeatured: {}", e.getMessage(), e);
            throw new AppException("Failed to retrieve featured products", e);
        }
        return products;
    }

    @Override
    public List<Product> findByCategory(String category, int limit) {
        List<Product> products = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CATEGORY)) {
            ps.setString(1, category);
            ps.setInt(2, limit > 0 ? limit : 12);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findByCategory: {}", e.getMessage(), e);
            throw new AppException("Failed to retrieve products by category", e);
        }
        return products;
    }

    @Override
    public List<Product> findBySellerId(Long sellerId) {
        List<Product> products = new ArrayList<>();
        if (sellerId == null) {
            return products;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SELLER)) {
            ps.setLong(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findBySellerId for {}: {}", sellerId, e.getMessage(), e);
            throw new AppException("Failed to retrieve products for seller", e);
        }
        return products;
    }

    @Override
    public List<String> findAllCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL_CATEGORIES);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            LOGGER.error("Database error in findAllCategories: {}", e.getMessage(), e);
            throw new AppException("Failed to retrieve categories", e);
        }
        return categories;
    }

    @Override
    public Product create(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, product.getSellerId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStockQty());
            ps.setString(6, product.getCategory());
            ps.setString(7, product.getImageUrl());
            ps.setBigDecimal(8, product.getAvgRating() != null ? product.getAvgRating() : BigDecimal.ZERO);
            ps.setBoolean(9, product.isActive());
            ps.setTimestamp(10, now);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AppException("Failed to create product: no rows affected.");
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                    product.setCreatedAt(now);
                    return product;
                } else {
                    throw new AppException("Failed to retrieve generated ID for product.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database error creating product {}: {}", product.getName(), e.getMessage(), e);
            throw new AppException("Failed to persist product", e);
        }
    }

    @Override
    public boolean update(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("Product and product ID cannot be null");
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getStockQty());
            ps.setString(5, product.getCategory());
            ps.setString(6, product.getImageUrl());
            ps.setBoolean(7, product.isActive());
            ps.setLong(8, product.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating product {}: {}", product.getId(), e.getMessage(), e);
            throw new AppException("Failed to update product", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error deleting product {}: {}", id, e.getMessage(), e);
            throw new AppException("Failed to delete product", e);
        }
    }

    @Override
    public boolean updateStock(Long productId, int quantityDelta, Connection conn) {
        if (productId == null || quantityDelta <= 0) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STOCK)) {
            ps.setInt(1, quantityDelta);
            ps.setLong(2, productId);
            ps.setInt(3, quantityDelta); // Ensures stock_qty >= quantityDelta

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating stock for product {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to update product stock", e);
        }
    }

    @Override
    public boolean restock(Long productId, int quantity, Connection conn) {
        if (productId == null || quantity <= 0) {
            return false;
        }
        String sql = "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error restocking product {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to restock product", e);
        }
    }

    @Override
    public boolean updateAvgRating(Long productId, BigDecimal newAvgRating, Connection conn) {
        if (productId == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_AVG_RATING)) {
            ps.setBigDecimal(1, newAvgRating != null ? newAvgRating : BigDecimal.ZERO);
            ps.setLong(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Database error updating avg rating for product {}: {}", productId, e.getMessage(), e);
            throw new AppException("Failed to update average rating", e);
        }
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
            LOGGER.error("Database error in countAll products: {}", e.getMessage(), e);
            throw new AppException("Failed to count all products", e);
        }
        return 0;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setAvgRating(rs.getBigDecimal("avg_rating"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            p.setSellerName(rs.getString("seller_name"));
        } catch (SQLException ignored) {
            // Column may not be in projection
        }
        return p;
    }
}
