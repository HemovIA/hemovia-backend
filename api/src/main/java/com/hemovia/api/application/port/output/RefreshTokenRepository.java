package com.hemovia.api.application.port.output;

import com.hemovia.api.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/*
Porta de saída: como a aplicação guarda e revoga refresh tokens.
*/
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Revoga todos os tokens ainda válidos de uma sessão (usado quando um token revogado é reapresentado)
    void revokeFamily(UUID familyId, Instant now);

    // Revoga todos os tokens ainda válidos de um usuário (usado ao desativá-lo)
    void revokeAllOfUser(UUID userId, Instant now);
}
