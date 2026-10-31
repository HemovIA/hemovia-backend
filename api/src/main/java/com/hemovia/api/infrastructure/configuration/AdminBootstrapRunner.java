package com.hemovia.api.infrastructure.configuration;

import com.hemovia.api.application.port.input.BootstrapAdminUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/*
Na subida da aplicação, pede ao caso de uso para criar o primeiro ADMIN, se ainda não existir nenhum.
*/
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final BootstrapAdminUseCase bootstrapAdmin;

    public AdminBootstrapRunner(BootstrapAdminUseCase bootstrapAdmin) {
        this.bootstrapAdmin = bootstrapAdmin;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrapAdmin.execute();
    }
}
