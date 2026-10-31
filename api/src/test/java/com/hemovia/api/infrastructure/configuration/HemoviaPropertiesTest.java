package com.hemovia.api.infrastructure.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 11 da Seção 1: a aplicação não sobe sem um JWT_SECRET válido.
Usamos o ApplicationContextRunner, que monta só o necessário para ligar as propriedades.
*/
class HemoviaPropertiesTest {

    // Segredo com exatamente o tamanho mínimo aceito
    private static final String SEGREDO_VALIDO = "s".repeat(32);

    // Contexto mínimo com as propriedades que não estão em teste, deixando de fora só o segredo
    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withUserConfiguration(HemoviaPropertiesConfiguration.class)
            .withPropertyValues(
                    "hemovia.security.access-token-minutes=15",
                    "hemovia.security.refresh-token-days=7",
                    "hemovia.security.cookie-secure=true",
                    "hemovia.security.cookie-same-site=Lax",
                    "hemovia.security.cors-allowed-origins=http://localhost:5173");

    // Mesmo contexto, mas lendo o application.yaml de verdade para provar que o arquivo está certo
    private final ApplicationContextRunner contextoComYaml = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(HemoviaPropertiesConfiguration.class);

    @Test
    void deveSubirQuandoOSegredoTemPeloMenos32Caracteres() {
        // Dado um segredo de 32 caracteres, quando o contexto sobe, então as propriedades ficam disponíveis
        contexto.withPropertyValues("hemovia.security.jwt-secret=" + SEGREDO_VALIDO)
                .run(ctx -> assertThat(ctx.getBean(HemoviaProperties.class).security().jwtSecret()).isEqualTo(SEGREDO_VALIDO));
    }

    @Test
    void deveFalharAoSubirSemJwtSecret() {
        // Quando ninguém informa o segredo, então o contexto não sobe e o erro cita o campo
        contexto.run(ctx -> assertThat(ctx).hasFailed().getFailure().hasStackTraceContaining("jwtSecret"));
    }

    @Test
    void deveFalharAoSubirComSegredoCurto() {
        // Dado um segredo de 31 caracteres, um a menos que o mínimo, então o contexto não sobe
        contexto.withPropertyValues("hemovia.security.jwt-secret=" + "s".repeat(31))
                .run(ctx -> assertThat(ctx).hasFailed().getFailure().hasStackTraceContaining("jwtSecret"));
    }

    @Test
    void deveCarregarOsPadroesDoApplicationYaml() {
        // Dado só o JWT_SECRET, as demais propriedades vêm dos valores padrão do application.yaml
        contextoComYaml.withPropertyValues("JWT_SECRET=" + SEGREDO_VALIDO).run(ctx -> {
            HemoviaProperties.Security seguranca = ctx.getBean(HemoviaProperties.class).security();
            // Access token de 15 minutos e refresh de 7 dias, como decidido no guia
            assertThat(seguranca.accessTokenMinutes()).isEqualTo(15);
            assertThat(seguranca.refreshTokenDays()).isEqualTo(7);
            // Cookie seguro por padrão e SameSite Lax
            assertThat(seguranca.cookieSecure()).isTrue();
            assertThat(seguranca.cookieSameSite()).isEqualTo("Lax");
            // CORS liberado só para o front local do Vite
            assertThat(seguranca.corsAllowedOrigins()).isEqualTo("http://localhost:5173");
        });
    }

    @Test
    void deveFalharComOYamlQuandoOJwtSecretEstaVazio() {
        // Quando JWT_SECRET existe, mas vazia (como num deploy mal configurado), o contexto não sobe
        // Usamos propriedade de teste, que vence a variável de ambiente que o Surefire define
        contextoComYaml.withPropertyValues("JWT_SECRET=")
                .run(ctx -> assertThat(ctx).hasFailed().getFailure().hasStackTraceContaining("jwtSecret"));
    }
}
