package com.hemovia.api.model.exception;

public final class RegistrationUnavailableException extends DomainException {
    public RegistrationUnavailableException() {
        super("O envio da confirmação está indisponível. Tente novamente mais tarde.");
    }

    @Override
    public String code() {
        return "REGISTRATION_UNAVAILABLE";
    }
}
