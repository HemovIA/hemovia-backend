package com.hemovia.api.service.usecase.user;

import com.hemovia.api.dto.response.UserResponse;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Busca um usuário pelo id.
*/
@Service
@Transactional(readOnly = true)
public class GetUser {

    private final UserRepository users;

    public GetUser(UserRepository users) {
        this.users = users;
    }

    public UserResponse execute(UUID userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
    }
}
