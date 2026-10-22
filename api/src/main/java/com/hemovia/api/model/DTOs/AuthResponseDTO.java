package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Token emitido após autenticação bem-sucedida.")
public class AuthResponseDTO {

    @Schema(description = "JWT para uso no botão Authorize do Swagger", example = "eyJhbGciOiJIUzI1NiJ9.exemplo.assinatura")
    private String token;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
