package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.request.UpdateUserRequest;
import com.hemovia.api.dto.response.UserResponse;

import java.util.UUID;

/*
Porta de entrada: Atualiza nome, telefone e e-mail de um usuário.
*/
public interface UpdateUserUseCase {

    UserResponse execute(UUID userId, UpdateUserRequest request);
}
