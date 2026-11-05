package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.infrastructure.persistence.mapper.RefreshTokenPersistenceMapper;
import com.hemovia.api.infrastructure.persistence.repository.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/*
Liga a porta de refresh tokens da aplicação ao Spring Data JPA.
*/
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository jpa;

    public RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return RefreshTokenPersistenceMapper.toDomain(jpa.save(RefreshTokenPersistenceMapper.toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(RefreshTokenPersistenceMapper::toDomain);
    }

    @Override
    public void revokeFamily(UUID familyId, Instant now) {
        jpa.revokeFamily(familyId, now);
    }

    @Override
    public void revokeAllOfUser(UUID userId, Instant now) {
        jpa.revokeAllOfUser(userId, now);
    }
}
