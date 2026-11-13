package com.hemovia.api.service.usecase.inventory;

import com.hemovia.api.dto.response.BloodBagResponse;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RhFactor;

import java.time.LocalDate;

final class InventoryBloodBags {
    private InventoryBloodBags() { }

    static void applyDetails(BloodBag bag, BloodType type, String rhFactor, String component,
                             LocalDate collected, LocalDate expires, BloodBagStatus status) {
        bag.setRhFactor(parse(RhFactor.class, rhFactor, "Rh factor"));
        bag.setComponent(parse(BloodComponent.class, component, "blood component"));
        bag.setCollectionDate(collected);
        bag.setExpirationDate(expires);
        bag.setStatus(status);
        bag.setBloodTypeEnum(type);
    }

    static BloodBagResponse response(BloodBag bag) {
        return response(bag, true);
    }

    static BloodBagResponse donationResponse(BloodBag bag) {
        return response(bag, false);
    }

    private static BloodBagResponse response(BloodBag bag, boolean includeInventoryDetails) {
        return new BloodBagResponse(bag.getId(), label(bag.getBloodTypeEnum()), label(bag.getRhFactor()),
                label(bag.getExpirationDate()), label(bag.getComponent()), label(bag.getCollectionDate()),
                includeInventoryDetails ? label(bag.getStatus()) : null,
                includeInventoryDetails && bag.getDonor() != null ? bag.getDonor().getId() : null,
                includeInventoryDetails && bag.getBloodCenter() != null ? bag.getBloodCenter().getId() : null);
    }

    private static String label(Object value) {
        return value == null ? null : value.toString();
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unknown " + field + ": " + value, e);
        }
    }
}
