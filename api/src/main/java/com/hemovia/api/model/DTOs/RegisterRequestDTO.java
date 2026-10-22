package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Dados de entrada para cadastrar um usuário.")
public class RegisterRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Ana Souza")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail com formato inválido")
    @Schema(format = "email", requiredMode = Schema.RequiredMode.REQUIRED, example = "ana.souza@example.test")
    private String email;

    @NotBlank(message = "O CPF é obrigatório")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678909")
    private String cpf;

    @Schema(example = "+5511999999999")
    private String phoneNumber;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    @Schema(format = "password", writeOnly = true, minLength = 6, requiredMode = Schema.RequiredMode.REQUIRED, example = "senha-sintetica")
    private String password;

    public RegisterRequestDTO() {
    }

    public RegisterRequestDTO(String name, String email, String cpf, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
