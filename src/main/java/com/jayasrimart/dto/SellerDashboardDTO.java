package com.jayasrimart.dto;

import com.jayasrimart.model.Order;
import com.jayasrimart.model.Product;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object encapsulating summary statistics and lists for the Seller Portal Dashboard.
 */
public class SellerDashboardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalProducts;
    private int lowStockCount;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private List<Order> recentOrders;
    private List<Product> lowStockProducts;

    /**
     * Default constructor.
     */
    public SellerDashboardDTO() {
        this.totalRevenue = BigDecimal.ZERO;
        this.recentOrders = Collections.emptyList();
        this.lowStockProducts = Collections.emptyList();
    }

    /**
     * Parameterized constructor.
     *
     * @param totalProducts total products listed by seller
     * @param lowStockCount number of products with stock <= 5
     * @param totalOrders total incoming orders containing seller products
     * @param totalRevenue total revenue generated (excluding cancelled orders)
     * @param recentOrders list of recent incoming orders
     * @param lowStockProducts list of products needing restock
     */
    public SellerDashboardDTO(int totalProducts, int lowStockCount, int totalOrders,
                              BigDecimal totalRevenue, List<Order> recentOrders,
                              List<Product> lowStockProducts) {
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.recentOrders = recentOrders != null ? recentOrders : Collections.emptyList();
        this.lowStockProducts = lowStockProducts != null ? lowStockProducts : Collections.emptyList();
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<Product> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }
}
