package com.hemovia.api.model.valueobject;

import com.hemovia.api.model.exception.InvalidRouteException;

public record GeoPoint(double latitude, double longitude) {
    public GeoPoint {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new InvalidRouteException("Coordenadas geográficas inválidas.");
        }
    }
}
