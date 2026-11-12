package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.request.LoginRequest;
import com.hemovia.api.dto.internal.SessionTokens;

/*
Porta de entrada: Entra no sistema com e-mail ou CPF e senha.
*/
public interface LoginUseCase {

    SessionTokens execute(LoginRequest request);
}
