package com.hemovia.api.model.enums;

/** Lifecycle state of an allocation that fulfils a blood request. */
public enum AllocationStatus {
    ALLOCATED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
