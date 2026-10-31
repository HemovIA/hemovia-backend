package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
Guarda senhas com BCrypt, que usa um sal aleatório por senha e é lento de propósito contra quem tenta adivinhar.
*/
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    // O codificador do Spring Security faz todo o trabalho pesado
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hash) {
        return encoder.matches(rawPassword, hash);
    }
}
