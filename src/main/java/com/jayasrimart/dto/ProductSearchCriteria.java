package com.jayasrimart.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Criteria object encapsulating search, filter, sorting, and pagination parameters
 * for product catalog queries.
 */
public class ProductSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private String sortBy; // "price_asc", "price_desc", "rating_desc", "newest"
    private Boolean activeOnly = Boolean.TRUE;
    private Long sellerId;
    private int limit = 12;
    private int offset = 0;

    public ProductSearchCriteria() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinRating() {
        return minRating;
    }

    public void setMinRating(BigDecimal minRating) {
        this.minRating = minRating;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Boolean getActiveOnly() {
        return activeOnly;
    }

    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Fluent Builder for {@link ProductSearchCriteria}.
     */
    public static class Builder {
        private final ProductSearchCriteria criteria = new ProductSearchCriteria();

        public Builder keyword(String keyword) {
            criteria.setKeyword(keyword);
            return this;
        }

        public Builder category(String category) {
            criteria.setCategory(category);
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            criteria.setMinPrice(minPrice);
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            criteria.setMaxPrice(maxPrice);
            return this;
        }

        public Builder minRating(BigDecimal minRating) {
            criteria.setMinRating(minRating);
            return this;
        }

        public Builder sortBy(String sortBy) {
            criteria.setSortBy(sortBy);
            return this;
        }

        public Builder activeOnly(Boolean activeOnly) {
            criteria.setActiveOnly(activeOnly);
            return this;
        }

        public Builder sellerId(Long sellerId) {
            criteria.setSellerId(sellerId);
            return this;
        }

        public Builder limit(int limit) {
            criteria.setLimit(limit);
            return this;
        }

        public Builder offset(int offset) {
            criteria.setOffset(offset);
            return this;
        }

        public ProductSearchCriteria build() {
            return criteria;
        }
    }
}
