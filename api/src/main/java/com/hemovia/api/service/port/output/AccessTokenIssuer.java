package com.hemovia.api.service.port.output;

import com.hemovia.api.dto.internal.IssuedToken;
import com.hemovia.api.model.AuthenticatedUser;
import com.hemovia.api.model.User;

/*
Porta de saída: emite o access token de um usuário e confere um token recebido.
*/
public interface AccessTokenIssuer {

    IssuedToken issue(User user);

    // Devolve quem é o dono do token, ou lança InvalidCredentialsException se ele for inválido ou estiver vencido
    AuthenticatedUser verify(String token);
}
