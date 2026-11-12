package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.response.UserResponse;

import java.util.List;

/*
Porta de entrada: Lista todos os usuários.
*/
public interface ListUsersUseCase {

    List<UserResponse> execute();
}
