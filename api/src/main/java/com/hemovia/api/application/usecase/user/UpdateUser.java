package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.dto.request.UpdateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.UpdateUserUseCase;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Atualiza nome, e-mail e telefone. Trocar o e-mail por um que já é de outra pessoa é recusado.
*/
@Service
@Transactional
public class UpdateUser implements UpdateUserUseCase {

    private final UserRepository users;

    public UpdateUser(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserResponse execute(UUID userId, UpdateUserRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        Email email = new Email(request.email());
        // Manter o próprio e-mail não é duplicidade
        if (!email.equals(user.email()) && users.existsByEmail(email)) {
            throw new DuplicateResourceException("E-mail já cadastrado");
        }
        user.updateProfile(request.name(), email, request.phoneNumber());
        return UserResponse.from(users.save(user));
    }
}
