package com.hemovia.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmDonorRegistrationRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "O link de confirmação é inválido.") String token,
        @NotBlank(message = "Informe seu nome.") @Size(max = 255) String name,
        @NotBlank(message = "Informe seu CPF.") @Size(max = 14) String cpf,
        @NotBlank(message = "Informe uma senha.") @Size(min = 8, max = 72, message = "Use entre 8 e 72 caracteres na senha.")
        String password) { }
