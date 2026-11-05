package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;

/*
Porta de entrada: Entra no sistema com e-mail ou CPF e senha.
*/
public interface LoginUseCase {

    SessionTokens execute(LoginRequest request);
}
