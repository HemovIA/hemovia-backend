package com.hemovia.api.service.port.input;

import com.hemovia.api.dto.internal.SessionTokens;

/*
Porta de entrada: Troca um refresh token válido por um par de tokens novo (rotação).
*/
public interface RefreshSessionUseCase {

    SessionTokens execute(String rawRefreshToken);
}
