package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.application.dto.response.UserResponse;

import java.util.UUID;

/*
Porta de entrada: Troca o papel e o vínculo de um usuário.
*/
public interface ChangeUserRoleUseCase {

    UserResponse execute(UUID userId, ChangeUserRoleRequest request);
}
