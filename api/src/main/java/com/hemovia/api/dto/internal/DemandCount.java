package com.hemovia.api.dto.internal;

import java.util.UUID;

public record DemandCount(UUID hospitalId, String hospitalName, int quantity, long count) { }
