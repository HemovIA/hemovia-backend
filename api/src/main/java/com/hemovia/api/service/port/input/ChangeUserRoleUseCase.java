package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.dto.response.UserResponse;

import java.util.UUID;

/*
Porta de entrada: Troca o papel e o vínculo de um usuário.
*/
public interface ChangeUserRoleUseCase {

    UserResponse execute(UUID userId, ChangeUserRoleRequest request);
}
