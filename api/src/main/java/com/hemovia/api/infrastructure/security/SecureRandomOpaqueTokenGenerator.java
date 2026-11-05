package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.OpaqueTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/*
Gera o refresh token: 32 bytes aleatórios de um gerador criptográfico, em Base64 URL sem preenchimento (43 caracteres).
*/
@Component
public class SecureRandomOpaqueTokenGenerator implements OpaqueTokenGenerator {

    // Quantidade de bytes aleatórios do token
    private static final int BYTES = 32;

    // Gerador criptograficamente seguro, compartilhado e seguro para várias threads
    private final SecureRandom random = new SecureRandom();

    @Override
    public String newToken() {
        byte[] bytes = new byte[BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
