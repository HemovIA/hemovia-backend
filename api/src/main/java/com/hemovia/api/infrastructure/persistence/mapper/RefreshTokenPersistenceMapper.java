package com.hemovia.api.infrastructure.persistence.mapper;

import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.infrastructure.persistence.entity.RefreshTokenEntity;

/*
Traduz entre o refresh token do domínio e a linha do banco.
*/
public final class RefreshTokenPersistenceMapper {

    private RefreshTokenPersistenceMapper() {
    }

    public static RefreshTokenEntity toEntity(RefreshToken token) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(token.id());
        entity.setUserId(token.userId());
        entity.setFamilyId(token.familyId());
        entity.setTokenHash(token.tokenHash());
        entity.setCreatedAt(token.createdAt());
        entity.setExpiresAt(token.expiresAt());
        entity.setRevokedAt(token.revokedAt());
        entity.setReplacedBy(token.replacedBy());
        return entity;
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.restore(entity.getId(), entity.getUserId(), entity.getFamilyId(), entity.getTokenHash(),
                entity.getCreatedAt(), entity.getExpiresAt(), entity.getRevokedAt(), entity.getReplacedBy());
    }
}
