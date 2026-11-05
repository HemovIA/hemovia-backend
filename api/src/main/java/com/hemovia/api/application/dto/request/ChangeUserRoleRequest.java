package com.hemovia.api.application.dto.request;

import com.hemovia.api.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/*
Novo papel de um usuário e o vínculo que ele exige.
*/
public record ChangeUserRoleRequest(
        @NotNull(message = "O papel é obrigatório") Role role,
        UUID hospitalId,
        UUID bloodCenterId,
        UUID vehicleId) { }
