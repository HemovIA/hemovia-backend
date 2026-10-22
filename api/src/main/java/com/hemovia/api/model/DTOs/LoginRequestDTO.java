package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Credenciais para autenticação. Informe email, CPF ou login, além da senha.")
public class LoginRequestDTO {

    @Schema(format = "email", example = "ana.souza@example.test")
    private String email;
    @Schema(example = "12345678909")
    private String cpf;
    @Schema(example = "ana.souza")
    private String login;

    @NotBlank(message = "A senha é obrigatória")
    @Schema(format = "password", writeOnly = true, requiredMode = Schema.RequiredMode.REQUIRED, example = "senha-sintetica")
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String email, String cpf, String login, String password) {
        this.email = email;
        this.cpf = cpf;
        this.login = login;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentifier() {
        if (login != null && !login.trim().isEmpty()) {
            return login.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (cpf != null && !cpf.trim().isEmpty()) {
            return cpf.trim();
        }
        return null;
    }
}
