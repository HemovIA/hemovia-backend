package com.hemovia.api.application.dto.request;

import com.hemovia.api.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/*
Dados para criar um usuário. Só o vínculo que combina com o papel deve vir preenchido.
*/
public record CreateUserRequest(
        @NotBlank(message = "O nome é obrigatório") String name,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail com formato inválido") String email,
        @NotBlank(message = "O CPF é obrigatório") String cpf,
        String phoneNumber,
        @NotBlank(message = "A senha é obrigatória") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String password,
        @NotNull(message = "O papel é obrigatório") Role role,
        UUID hospitalId,
        UUID bloodCenterId,
        UUID vehicleId) { }
