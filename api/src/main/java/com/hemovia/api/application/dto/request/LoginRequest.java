package com.hemovia.api.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/*
Dados do login: o identificador é o e-mail ou o CPF, no mesmo campo.
*/
public record LoginRequest(
        @NotBlank(message = "O e-mail ou CPF é obrigatório") String identifier,
        @NotBlank(message = "A senha é obrigatória") String password) { }
