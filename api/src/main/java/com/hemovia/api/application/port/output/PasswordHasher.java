package com.hemovia.api.application.port.output;

/*
Porta de saída: transforma a senha em hash e confere uma senha contra o hash guardado.
*/
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
