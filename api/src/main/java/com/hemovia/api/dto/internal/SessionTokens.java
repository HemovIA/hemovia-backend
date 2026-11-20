package com.hemovia.api.dto.internal;

import com.hemovia.api.dto.response.UserResponse;

import java.time.Instant;

/*
Resultado de um login ou refresh: os dois tokens e o usuário.
Os tokens vão para cookies HttpOnly; eles nunca entram no corpo da resposta HTTP.
*/
public record SessionTokens(String accessToken, Instant accessExpiresAt,
                            String refreshToken, Instant refreshExpiresAt, UserResponse user) { }
