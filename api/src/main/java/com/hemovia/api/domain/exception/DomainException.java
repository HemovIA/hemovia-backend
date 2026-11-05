package com.hemovia.api.domain.exception;

/*
Base de todas as exceções de negócio do sistema.
O domínio não sabe de HTTP: ele só entrega um código estável, e a apresentação decide o status.
*/
public abstract class DomainException extends RuntimeException {

    // Toda exceção de negócio nasce com uma mensagem em português, pensada para quem consome a API
    protected DomainException(String message) {
        super(message);
    }

    // Código estável que identifica o tipo de erro, como NOT_FOUND ou INSUFFICIENT_INVENTORY
    public abstract String code();
}
