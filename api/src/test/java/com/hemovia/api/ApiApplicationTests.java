package com.hemovia.api;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

// Sobe o contexto completo (precisa do PostgreSQL): roda na esteira, não no build da imagem
@Tag("integration")
@SpringBootTest
class ApiApplicationTests {

    // Flyway da própria aplicação, configurado como em produção
    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
        // Se o contexto subiu, o Flyway e o mapeamento JPA aceitaram o banco
        assertThat(flyway).isNotNull();
    }

    @Test
    void deveTerAplicadoAsMigrationsAteAV13AoSubirOContexto() {
        // Ciclo 4 da Seção 2: ao subir, a aplicação deixa o banco na última versão do guia
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("13");
    }
}
