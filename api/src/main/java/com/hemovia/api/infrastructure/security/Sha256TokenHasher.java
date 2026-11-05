package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.TokenHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/*
Resume o token em SHA-256, em hexadecimal minúsculo (64 caracteres). Como o token já é aleatório e longo, não precisa de sal.
*/
@Component
public class Sha256TokenHasher implements TokenHasher {

    @Override
    public String sha256(String rawToken) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException erro) {
            // Todo Java traz SHA-256; se faltar, o ambiente está quebrado e não adianta continuar
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", erro);
        }
    }
}
