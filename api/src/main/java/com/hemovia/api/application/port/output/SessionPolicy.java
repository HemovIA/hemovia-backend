package com.hemovia.api.application.port.output;

import java.time.Duration;

/*
Porta de saída: as durações que a configuração define para a sessão.
*/
public interface SessionPolicy {

    // Quanto tempo um refresh token vale
    Duration refreshTokenTtl();
}
