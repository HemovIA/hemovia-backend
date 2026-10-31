package com.hemovia.api.domain.exception;

/*
Lançada quando alguém tenta cadastrar ou usar uma bolsa que já passou da validade.
*/
public final class BloodBagExpiredException extends DomainException {

    // A mensagem é sempre a mesma, porque a regra é uma só
    public BloodBagExpiredException() {
        super("A data de validade da bolsa deve estar no futuro.");
    }

    // Código estável que a apresentação traduz em status HTTP
    @Override
    public String code() {
        return "BLOOD_BAG_EXPIRED";
    }
}
