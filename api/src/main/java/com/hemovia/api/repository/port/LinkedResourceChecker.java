package com.hemovia.api.repository.port;

import java.util.UUID;

/*
Porta de saída: confere se o hospital, hemocentro ou veículo do vínculo de um usuário existe.
*/
public interface LinkedResourceChecker {

    boolean hospitalExists(UUID id);

    boolean bloodCenterExists(UUID id);

    boolean vehicleExists(UUID id);
}
