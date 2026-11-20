package com.hemovia.api.model.exception;

public final class InvalidRouteException extends DomainException {
    public InvalidRouteException(String message) { super(message); }
    @Override public String code() { return "INVALID_ROUTE"; }
}
