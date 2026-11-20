package com.hemovia.api.dto.internal;

import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.model.enums.RhFactor;

public record InventoryCount(BloodType bloodType, RhFactor rhFactor, BloodComponent component, long count) { }
