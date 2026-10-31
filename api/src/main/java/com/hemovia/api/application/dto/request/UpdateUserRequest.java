package com.hemovia.api.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
Dados que o ADMIN pode corrigir num usuário: nome, e-mail e telefone.
*/
public record UpdateUserRequest(
        @NotBlank(message = "O nome é obrigatório") String name,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail com formato inválido") String email,
        String phoneNumber) { }
