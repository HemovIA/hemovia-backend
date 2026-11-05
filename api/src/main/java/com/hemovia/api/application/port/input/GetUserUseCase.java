package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.response.UserResponse;

import java.util.UUID;

/*
Porta de entrada: Busca um usuário pelo id.
*/
public interface GetUserUseCase {

    UserResponse execute(UUID userId);
}
