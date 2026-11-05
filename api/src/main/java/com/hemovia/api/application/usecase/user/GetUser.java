package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.application.port.input.GetUserUseCase;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/*
Busca um usuário pelo id.
*/
@Service
@Transactional(readOnly = true)
public class GetUser implements GetUserUseCase {

    private final UserRepository users;

    public GetUser(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserResponse execute(UUID userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
    }
}
