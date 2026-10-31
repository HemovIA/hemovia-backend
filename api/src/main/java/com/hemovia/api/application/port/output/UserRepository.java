package com.hemovia.api.application.port.output;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
Porta de saída: como a aplicação guarda e busca usuários, sem saber de banco.
*/
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    Optional<User> findByCpf(Cpf cpf);

    boolean existsByEmail(Email email);

    boolean existsByCpf(Cpf cpf);

    boolean existsByRole(Role role);

    List<User> findAll();

    User save(User user);
}
