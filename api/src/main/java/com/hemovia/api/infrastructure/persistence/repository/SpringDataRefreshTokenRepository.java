package com.hemovia.api.infrastructure.persistence.repository;

import com.hemovia.api.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/*
Consultas do Spring Data para os refresh tokens. As revogações em lote ainda não revogados mantêm a primeira data de revogação.
*/
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    // Revoga de uma vez todos os tokens ainda válidos de uma família; limpa o cache para as próximas leituras verem o banco
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    // Revoga de uma vez todos os tokens ainda válidos de um usuário
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RefreshTokenEntity t set t.revokedAt = :now where t.userId = :userId and t.revokedAt is null")
    int revokeAllOfUser(@Param("userId") UUID userId, @Param("now") Instant now);
}
