package com.hemovia.api.domain.exception;

/*
Lançada quando o login falha. A mensagem nunca revela se foi o usuário ou a senha que errou.
*/
public final class InvalidCredentialsException extends DomainException {

    // A mensagem vem de quem validou o login
    public InvalidCredentialsException(String message) {
        super(message);
    }

    // Código estável que a apresentação traduz em status HTTP
    @Override
    public String code() {
        return "UNAUTHORIZED";
    }
}
