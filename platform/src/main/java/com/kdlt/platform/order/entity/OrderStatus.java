package com.kdlt.platform.order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    IN_PRODUCTION,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    // Parcours normal : PENDING → CONFIRMED → IN_PRODUCTION → SHIPPED → DELIVERED.
    // Une commande peut être annulée tant qu'elle n'est pas expédiée.
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == IN_PRODUCTION || next == CANCELLED;
            case IN_PRODUCTION -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}