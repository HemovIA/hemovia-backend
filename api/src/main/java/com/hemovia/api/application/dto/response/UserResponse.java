package com.hemovia.api.application.dto.response;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;

import java.util.UUID;

/*
Dados públicos de um usuário. A senha e o hash dela nunca saem.
*/
public record UserResponse(UUID id, String name, String email, String cpf, String phoneNumber, Role role,
                           boolean active, UUID hospitalId, UUID bloodCenterId, UUID vehicleId) {

    // Monta a resposta a partir do usuário de domínio
    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.email().value(), user.cpf().digits(), user.phoneNumber(),
                user.role(), user.isActive(), user.hospitalId(), user.bloodCenterId(), user.vehicleId());
    }
}
