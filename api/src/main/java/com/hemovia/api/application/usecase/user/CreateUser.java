package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.dto.request.CreateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.CreateUserUseCase;
import com.hemovia.api.application.port.output.PasswordHasher;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Cria um usuário: valida e normaliza e-mail e CPF, recusa duplicados, confere o vínculo e guarda a senha só em hash.
*/
@Service
@Transactional
public class CreateUser implements CreateUserUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final UserLinkValidator links;

    public CreateUser(UserRepository users, PasswordHasher hasher, UserLinkValidator links) {
        this.users = users;
        this.hasher = hasher;
        this.links = links;
    }

    @Override
    public UserResponse execute(CreateUserRequest request) {
        // Os objetos de valor já validam e normalizam os dados
        Email email = new Email(request.email());
        Cpf cpf = new Cpf(request.cpf());
        if (users.existsByEmail(email)) {
            throw new DuplicateResourceException("E-mail já cadastrado");
        }
        if (users.existsByCpf(cpf)) {
            throw new DuplicateResourceException("CPF já cadastrado");
        }
        UUID linkedId = links.validate(request.role(), request.hospitalId(), request.bloodCenterId(), request.vehicleId());
        User user = User.create(request.name(), email, cpf, request.phoneNumber(),
                hasher.hash(request.password()), request.role(), linkedId);
        return UserResponse.from(users.save(user));
    }
}
