package com.hemovia.api.application.port.output;

import com.hemovia.api.domain.model.AuthenticatedUser;

/*
Porta de saída: quem é o usuário autenticado na requisição atual.
*/
public interface CurrentUserProvider {

    // Lança InvalidCredentialsException se não houver ninguém autenticado
    AuthenticatedUser current();
}
