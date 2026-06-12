package com.agrolink.backend.dto;

public class AdminDashboardResponse {

    private final long userCount;
    private final long productCount;
    private final long orderCount;
    private final double totalRevenue;

    public AdminDashboardResponse(long userCount, long productCount, long orderCount, double totalRevenue) {
        this.userCount = userCount;
        this.productCount = productCount;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getProductCount() {
        return productCount;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public long getUsers() {
        return userCount;
    }

    public long getProducts() {
        return productCount;
    }

    public long getOrders() {
        return orderCount;
    }

    public double getRevenue() {
        return totalRevenue;
    }

    public long getTotalUsers() {
        return userCount;
    }

    public long getTotalProducts() {
        return productCount;
    }

    public long getTotalOrders() {
        return orderCount;
    }
}
