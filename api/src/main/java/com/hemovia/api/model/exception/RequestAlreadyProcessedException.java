package com.hemovia.api.model.exception;

public final class RequestAlreadyProcessedException extends DomainException {
    public RequestAlreadyProcessedException() { super("A requisição já foi processada."); }
    @Override public String code() { return "REQUEST_ALREADY_PROCESSED"; }
}
