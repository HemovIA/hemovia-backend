package com.hemovia.api.application.port.input;

/*
Porta de entrada: Encerra a sessão revogando o refresh token atual.
*/
public interface LogoutUseCase {

    void execute(String rawRefreshToken);
}
