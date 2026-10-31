package com.hemovia.api.domain.exception;

/*
Lançada quando e-mail, CPF ou código já existe e não pode se repetir.
*/
public final class DuplicateResourceException extends DomainException {

    // A mensagem já vem pronta de quem detectou a duplicidade, por exemplo "E-mail já cadastrado"
    public DuplicateResourceException(String message) {
        super(message);
    }

    // Código estável que a apresentação traduz em status HTTP
    @Override
    public String code() {
        return "DUPLICATE_RESOURCE";
    }
}
