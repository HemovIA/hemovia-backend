package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.response.UserResponse;

/*
Porta de entrada: Devolve os dados do usuário autenticado.
*/
public interface GetCurrentUserUseCase {

    UserResponse execute();
}
