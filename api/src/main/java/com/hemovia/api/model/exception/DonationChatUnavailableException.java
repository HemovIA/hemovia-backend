package com.hemovia.api.model.exception;

public class DonationChatUnavailableException extends DomainException {

    public DonationChatUnavailableException() {
        super("O assistente está temporariamente indisponível. Tente novamente mais tarde.");
    }

    @Override
    public String code() {
        return "DONATION_CHAT_UNAVAILABLE";
    }
}

