package com.hemovia.api.infrastructure.persistence.repository;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/*
Consultas prontas do Spring Data para a tabela de usuários.
*/
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByRole(Role role);
}
