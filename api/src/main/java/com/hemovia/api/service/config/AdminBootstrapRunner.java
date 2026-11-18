package com.hemovia.api.service.config;

import com.hemovia.api.service.usecase.user.BootstrapAdmin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/*
Na subida da aplicação, pede ao caso de uso para criar o primeiro ADMIN, se ainda não existir nenhum.
*/
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final BootstrapAdmin bootstrapAdmin;

    public AdminBootstrapRunner(BootstrapAdmin bootstrapAdmin) {
        this.bootstrapAdmin = bootstrapAdmin;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrapAdmin.execute();
    }
}
