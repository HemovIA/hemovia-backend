package com.hemovia.api.infrastructure.persistence.mapper;

import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import com.hemovia.api.infrastructure.persistence.entity.UserEntity;

/*
Traduz entre o usuário do domínio e a linha do banco.
*/
public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setName(user.name());
        entity.setEmail(user.email().value());
        entity.setCpf(user.cpf().digits());
        entity.setPhoneNumber(user.phoneNumber());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role());
        entity.setActive(user.isActive());
        entity.setHospitalId(user.hospitalId());
        entity.setBloodCenterId(user.bloodCenterId());
        entity.setVehicleId(user.vehicleId());
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        return User.restore(entity.getId(), entity.getName(), new Email(entity.getEmail()), new Cpf(entity.getCpf()),
                entity.getPhoneNumber(), entity.getPasswordHash(), entity.getRole(), entity.isActive(),
                entity.getHospitalId(), entity.getBloodCenterId(), entity.getVehicleId());
    }
}
