package com.hemovia.api.infrastructure.persistence.entity;

import com.hemovia.api.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/*
Como o usuário é guardado na tabela users. É só um molde do banco; as regras ficam no User do domínio.
*/
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    // O id nasce no domínio, por isso não há geração automática aqui
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Só os dígitos do CPF
    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(name = "phone_number")
    private String phoneNumber;

    // A coluna se chama password, mas guarda sempre o hash BCrypt
    @Column(name = "password", nullable = false)
    private String passwordHash;

    // Nulo para usuários antigos que ainda não ganharam papel
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "blood_center_id")
    private UUID bloodCenterId;

    @Column(name = "vehicle_id")
    private UUID vehicleId;
}
