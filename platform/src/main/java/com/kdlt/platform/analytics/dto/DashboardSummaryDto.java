package com.kdlt.platform.analytics.dto;

import java.math.BigDecimal;

public class DashboardSummaryDto {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long pendingOrders;
    private long confirmedOrders;
    private long inProductionOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private long pendingQuotes;
    private long totalQuotes;
    private long totalCustomers;
    private long totalActiveProducts;

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    public long getConfirmedOrders() { return confirmedOrders; }
    public void setConfirmedOrders(long confirmedOrders) { this.confirmedOrders = confirmedOrders; }
    public long getInProductionOrders() { return inProductionOrders; }
    public void setInProductionOrders(long inProductionOrders) { this.inProductionOrders = inProductionOrders; }
    public long getShippedOrders() { return shippedOrders; }
    public void setShippedOrders(long shippedOrders) { this.shippedOrders = shippedOrders; }
    public long getDeliveredOrders() { return deliveredOrders; }
    public void setDeliveredOrders(long deliveredOrders) { this.deliveredOrders = deliveredOrders; }
    public long getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    public long getPendingQuotes() { return pendingQuotes; }
    public void setPendingQuotes(long pendingQuotes) { this.pendingQuotes = pendingQuotes; }
    public long getTotalQuotes() { return totalQuotes; }
    public void setTotalQuotes(long totalQuotes) { this.totalQuotes = totalQuotes; }
    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }
    public long getTotalActiveProducts() { return totalActiveProducts; }
    public void setTotalActiveProducts(long totalActiveProducts) { this.totalActiveProducts = totalActiveProducts; }
}