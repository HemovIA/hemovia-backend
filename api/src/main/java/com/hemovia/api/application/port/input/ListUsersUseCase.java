package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.response.UserResponse;

import java.util.List;

/*
Porta de entrada: Lista todos os usuários.
*/
public interface ListUsersUseCase {

    List<UserResponse> execute();
}
