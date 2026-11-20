package com.hemovia.api.service.port.output;

/*
Porta de saída: gera o valor aleatório e imprevisível do refresh token.
*/
public interface OpaqueTokenGenerator {

    String newToken();
}
