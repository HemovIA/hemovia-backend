package com.hemovia.api.application.port.input;

/*
Porta de entrada: Cria o primeiro ADMIN a partir das variáveis de ambiente, se ainda não existir nenhum.
*/
public interface BootstrapAdminUseCase {

    void execute();
}
