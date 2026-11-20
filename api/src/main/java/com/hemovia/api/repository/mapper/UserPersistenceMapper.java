package com.hemovia.api.repository.mapper;

import com.hemovia.api.model.User;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.model.entity.UserEntity;

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
