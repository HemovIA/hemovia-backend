package com.hemovia.api.domain.exception;

/*
Lançada quando um dado de negócio não faz sentido, como um CPF com dígito errado ou um papel sem vínculo.
*/
public final class InvalidDataException extends DomainException {

    // A mensagem explica, em português, o que está errado
    public InvalidDataException(String message) {
        super(message);
    }

    // Mesmo código das validações de campo do DTO, porque para o cliente é o mesmo tipo de erro
    @Override
    public String code() {
        return "VALIDATION_ERROR";
    }
}
