package com.hemovia.api.application.port.input;

import com.hemovia.api.application.dto.response.SessionTokens;

/*
Porta de entrada: Troca um refresh token válido por um par de tokens novo (rotação).
*/
public interface RefreshSessionUseCase {

    SessionTokens execute(String rawRefreshToken);
}
