package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.request.CreateUserRequest;
import com.hemovia.api.dto.response.UserResponse;

/*
Porta de entrada: Cria um usuário (só o ADMIN faz isso).
*/
public interface CreateUserUseCase {

    UserResponse execute(CreateUserRequest request);
}
