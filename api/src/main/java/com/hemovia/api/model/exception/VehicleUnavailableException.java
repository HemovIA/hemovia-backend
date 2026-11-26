package com.hemovia.api.model.exception;

public final class VehicleUnavailableException extends DomainException {
    public VehicleUnavailableException() { super("O veículo já está em transporte."); }
    @Override public String code() { return "VEHICLE_UNAVAILABLE"; }
}
