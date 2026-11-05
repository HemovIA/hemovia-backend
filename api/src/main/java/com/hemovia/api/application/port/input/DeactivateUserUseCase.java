package com.hemovia.api.application.port.input;

import java.util.UUID;

/*
Porta de entrada: Desativa um usuário e revoga as sessões dele.
*/
public interface DeactivateUserUseCase {

    void execute(UUID userId);
}
