package com.hemovia.api.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/*
Refresh token de uma sessão. Só o hash do valor fica guardado, nunca o token puro.
Cada uso gera um token novo da mesma família e revoga o anterior; reusar um revogado indica roubo.
*/
public final class RefreshToken {

    // Identificador do token
    private final UUID id;
    // Dono do token
    private final UUID userId;
    // Todos os tokens da mesma sessão dividem a família
    private final UUID familyId;
    // SHA-256 do valor entregue ao cliente
    private final String tokenHash;
    // Quando foi emitido e quando vence
    private final Instant createdAt;
    private final Instant expiresAt;
    // Preenchidos quando o token é revogado
    private Instant revokedAt;
    private UUID replacedBy;

    private RefreshToken(UUID id, UUID userId, UUID familyId, String tokenHash,
                         Instant createdAt, Instant expiresAt, Instant revokedAt, UUID replacedBy) {
        this.id = id;
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.replacedBy = replacedBy;
    }

    // Emite um token novo, que vale pela duração informada a partir de "now"
    public static RefreshToken issue(UUID userId, UUID familyId, String tokenHash, Instant now, Duration ttl) {
        return new RefreshToken(UUID.randomUUID(), userId, familyId, tokenHash, now, now.plus(ttl), null, null);
    }

    // Reconstrói um token já guardado, sem validar nada de novo
    public static RefreshToken restore(UUID id, UUID userId, UUID familyId, String tokenHash,
                                       Instant createdAt, Instant expiresAt, Instant revokedAt, UUID replacedBy) {
        return new RefreshToken(id, userId, familyId, tokenHash, createdAt, expiresAt, revokedAt, replacedBy);
    }

    // Vale se não foi revogado e ainda não venceu (no instante exato do vencimento já não vale)
    public boolean isUsableAt(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    // Marca o token como revogado; a primeira revogação é a que fica registrada
    public void revoke(Instant now, UUID replacedBy) {
        if (revokedAt == null) {
            this.revokedAt = now;
            this.replacedBy = replacedBy;
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public UUID familyId() {
        return familyId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public UUID replacedBy() {
        return replacedBy;
    }
}
