package com.hemovia.api.application.dto.response;

import java.util.UUID;

public record BloodCenterResponse(UUID id, String name, String city, double latitude, double longitude) {
}
