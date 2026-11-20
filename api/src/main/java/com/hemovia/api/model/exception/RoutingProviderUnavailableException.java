package com.hemovia.api.model.exception;



public final class RoutingProviderUnavailableException extends DomainException {
    public RoutingProviderUnavailableException(String message) {
        super(message);
    }
    @Override public String code() {
        return "ROUTING_PROVIDER_UNAVAILABLE";
    }
}
