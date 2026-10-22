package com.hemovia.api.model.DTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "UserResponse", description = "Dados públicos do usuário; não inclui credenciais.")
public class UserResponseDTO {

    @Schema(format = "uuid", example = "3f89f29b-8d2a-43d5-98a9-d44f1c2c1b80")
    private UUID id;
    @Schema(example = "Ana Souza")
    private String name;
    @Schema(format = "email", example = "ana.souza@example.test")
    private String email;
    @Schema(example = "12345678909")
    private String cpf;
    @Schema(example = "+5511999999999")
    private String phoneNumber;

    public UserResponseDTO() {
    }

    public UserResponseDTO(UUID id, String name, String email, String cpf, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.phoneNumber = phoneNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}
