package com.kdlt.platform.order.event;

import com.kdlt.platform.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderStatusChangedEvent(
        Long orderId,
        String customerEmail,
        String customerFirstName,
        String customerLastName,
        OrderStatus status,
        List<Line> lines,
        BigDecimal total,
        List<String> staffEmails
) {
    public record Line(String productName, int quantity, BigDecimal lineTotal) {}
}