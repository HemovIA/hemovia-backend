package com.hemovia.api.infrastructure.security;

import com.hemovia.api.application.port.output.AdminBootstrapSettings;
import com.hemovia.api.infrastructure.configuration.HemoviaProperties;
import org.springframework.stereotype.Component;

/*
Entrega à aplicação os dados do primeiro ADMIN lidos das variáveis de ambiente ADMIN_*.
*/
@Component
public class ConfiguredAdminBootstrapSettings implements AdminBootstrapSettings {

    private final HemoviaProperties.AdminBootstrap admin;

    public ConfiguredAdminBootstrapSettings(HemoviaProperties properties) {
        this.admin = properties.adminBootstrap();
    }

    @Override
    public String name() {
        return admin.name();
    }

    @Override
    public String email() {
        return admin.email();
    }

    @Override
    public String cpf() {
        return admin.cpf();
    }

    @Override
    public String password() {
        return admin.password();
    }
}
