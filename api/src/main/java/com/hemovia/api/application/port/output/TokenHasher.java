package com.hemovia.api.application.port.output;

/*
Porta de saída: resume um token em SHA-256, para guardar só o resumo e nunca o token.
*/
public interface TokenHasher {

    String sha256(String rawToken);
}
