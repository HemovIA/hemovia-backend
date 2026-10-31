package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.request.CreateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;

/*
Porta de entrada: Cria um usuário (só o ADMIN faz isso).
*/
public interface CreateUserUseCase {

    UserResponse execute(CreateUserRequest request);
}
