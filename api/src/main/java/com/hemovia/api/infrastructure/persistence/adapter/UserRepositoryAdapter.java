package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import com.hemovia.api.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.hemovia.api.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
Liga a porta de usuários da aplicação ao Spring Data JPA.
*/
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository jpa;

    public UserRepositoryAdapter(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByCpf(Cpf cpf) {
        return jpa.findByCpf(cpf.digits()).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }

    @Override
    public boolean existsByCpf(Cpf cpf) {
        return jpa.existsByCpf(cpf.digits());
    }

    @Override
    public boolean existsByRole(Role role) {
        return jpa.existsByRole(role);
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll().stream().map(UserPersistenceMapper::toDomain).toList();
    }

    @Override
    public User save(User user) {
        // O id já existe, então o Spring Data faz merge: insere se for novo, atualiza se já existir
        return UserPersistenceMapper.toDomain(jpa.save(UserPersistenceMapper.toEntity(user)));
    }
}
