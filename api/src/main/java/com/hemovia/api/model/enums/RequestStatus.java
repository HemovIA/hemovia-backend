package com.hemovia.api.model.enums;

/** Lifecycle state of a hospital blood request. */
public enum RequestStatus {
    PENDING,
    ALLOCATED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
