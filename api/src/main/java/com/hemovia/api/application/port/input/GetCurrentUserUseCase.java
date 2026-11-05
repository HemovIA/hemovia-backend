package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.response.UserResponse;

/*
Porta de entrada: Devolve os dados do usuário autenticado.
*/
public interface GetCurrentUserUseCase {

    UserResponse execute();
}
