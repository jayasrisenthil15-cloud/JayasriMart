package com.jayasrimart.model;

/**
 * Lifecycle states of an order in JayasriMart.
 * Workflow: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED.
 * CANCELLED is permitted only from PENDING or CONFIRMED.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Determines whether a transition from this status to the target status is allowed.
     *
     * @param targetStatus the desired next status
     * @return true if valid transition, false otherwise
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        if (this == targetStatus) {
            return true;
        }
        return switch (this) {
            case PENDING -> targetStatus == CONFIRMED || targetStatus == CANCELLED;
            case CONFIRMED -> targetStatus == SHIPPED || targetStatus == CANCELLED;
            case SHIPPED -> targetStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
