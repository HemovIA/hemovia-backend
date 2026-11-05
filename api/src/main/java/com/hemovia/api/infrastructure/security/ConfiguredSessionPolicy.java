package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.SessionPolicy;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/*
Entrega à aplicação a duração do refresh token definida na configuração.
*/
@Component
public class ConfiguredSessionPolicy implements SessionPolicy {

    private final HemoviaProperties.Security security;

    public ConfiguredSessionPolicy(HemoviaProperties properties) {
        this.security = properties.security();
    }

    @Override
    public Duration refreshTokenTtl() {
        return Duration.ofDays(security.refreshTokenDays());
    }
}
