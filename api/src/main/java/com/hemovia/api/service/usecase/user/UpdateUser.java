package com.hemovia.api.service.usecase.user;

import com.hemovia.api.dto.request.UpdateUserRequest;
import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.model.User;
import com.hemovia.api.model.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Atualiza nome, e-mail e telefone. Trocar o e-mail por um que já é de outra pessoa é recusado.
*/
@Service
@Transactional
public class UpdateUser {

    private final UserRepository users;

    public UpdateUser(UserRepository users) {
        this.users = users;
    }

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
