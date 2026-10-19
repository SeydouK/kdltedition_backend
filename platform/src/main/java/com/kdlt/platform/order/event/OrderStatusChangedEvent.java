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
        Delivery delivery,
        List<String> staffEmails
) {
    public record Line(String productName, int quantity, BigDecimal lineTotal) {}

    // Peut être null pour une commande ancienne, passée avant l'ajout de l'adresse.
    public record Delivery(String phone, String address, String city, String notes,
                           BigDecimal latitude, BigDecimal longitude) {}
}