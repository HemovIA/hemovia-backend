package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.response.UserResponse;

import java.util.UUID;

/*
Porta de entrada: Busca um usuário pelo id.
*/
public interface GetUserUseCase {

    UserResponse execute(UUID userId);
}
