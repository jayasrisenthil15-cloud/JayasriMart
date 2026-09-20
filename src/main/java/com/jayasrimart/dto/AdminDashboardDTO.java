package com.jayasrimart.dto;

import com.jayasrimart.model.Order;
import com.jayasrimart.model.User;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object encapsulating platform-wide health metrics, counts, and recent records for the Admin Portal.
 */
public class AdminDashboardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalUsers;
    private int totalBuyers;
    private int totalSellers;
    private int totalAdmins;
    private int totalProducts;
    private int totalOrders;
    private BigDecimal totalRevenue;
    private List<User> recentUsers;
    private List<Order> recentOrders;

    /**
     * Default constructor.
     */
    public AdminDashboardDTO() {
        this.totalRevenue = BigDecimal.ZERO;
        this.recentUsers = Collections.emptyList();
        this.recentOrders = Collections.emptyList();
    }

    /**
     * Parameterized constructor.
     *
     * @param totalUsers total registered users
     * @param totalBuyers count of buyers
     * @param totalSellers count of sellers
     * @param totalAdmins count of administrators
     * @param totalProducts total active and inactive catalog products
     * @param totalOrders total platform orders placed
     * @param totalRevenue platform-wide Gross Merchandise Value (excluding cancelled)
     * @param recentUsers recently registered user accounts
     * @param recentOrders recently placed customer orders
     */
    public AdminDashboardDTO(int totalUsers, int totalBuyers, int totalSellers, int totalAdmins,
                             int totalProducts, int totalOrders, BigDecimal totalRevenue,
                             List<User> recentUsers, List<Order> recentOrders) {
        this.totalUsers = totalUsers;
        this.totalBuyers = totalBuyers;
        this.totalSellers = totalSellers;
        this.totalAdmins = totalAdmins;
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.recentUsers = recentUsers != null ? recentUsers : Collections.emptyList();
        this.recentOrders = recentOrders != null ? recentOrders : Collections.emptyList();
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalBuyers() {
        return totalBuyers;
    }

    public void setTotalBuyers(int totalBuyers) {
        this.totalBuyers = totalBuyers;
    }

    public int getTotalSellers() {
        return totalSellers;
    }

    public void setTotalSellers(int totalSellers) {
        this.totalSellers = totalSellers;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
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

    public List<User> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<User> recentUsers) {
        this.recentUsers = recentUsers;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
