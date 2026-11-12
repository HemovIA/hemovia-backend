package com.hemovia.api.controller.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
Descreve a API no OpenAPI (doc, seção 44) e alimenta o Swagger UI.
*/
@Configuration
public class OpenApiConfiguration {
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @Bean
    public OpenAPI hemoviaOpenApi() {
        // Título e versão aparecem no topo do Swagger UI; o servidor carrega o prefixo /api/v1
        return new OpenAPI()
                .info(new Info().title("Hemovia API").version("v1")
                        .description("Documentação da API Hemovia para gestão de doações e bolsas de sangue. Todos os dados são sintéticos."))
                .components(new Components().addSecuritySchemes("cookieAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name("HEMOVIA_ACCESS")
                        .description("Sessão HttpOnly criada por POST /api/v1/auth/login. Escritas exigem X-XSRF-TOKEN.")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .servers(List.of(new Server().url(ApiPrefixConfiguration.PREFIXO)));
    }

    @Bean
    public OpenApiCustomizer caminhosRelativosAoServidor() {
        // O springdoc lê os caminhos já com /api/v1; como o servidor também o traz, tiramos do caminho para o "Try it out" não duplicar
        return openApi -> {
            Paths relativos = new Paths();
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((caminho, item) -> {
                    String relativo = semPrefixo(caminho);
                    // Login, refresh e emissão de CSRF não exigem uma sessão de acesso ativa
                    if (List.of("/auth/login", "/auth/refresh", "/auth/csrf").contains(relativo)) {
                        item.readOperations().forEach(operation -> operation.setSecurity(List.of()));
                    }
                    // O login e a telemetria são isentos de CSRF na cadeia de segurança.
                    if (!List.of("/auth/login", "/telemetry").contains(relativo)) {
                        item.readOperationsMap().forEach((metodo, operacao) -> {
                            switch (metodo) {
                                case POST, PUT, PATCH, DELETE -> documentarCsrf(operacao);
                                default -> { }
                            }
                        });
                    }
                    relativos.addPathItem(relativo, item);
                });
            }
            openApi.setPaths(relativos);
        };
    }

    private static void documentarCsrf(Operation operacao) {
        var parametros = operacao.getParameters();
        if (parametros != null) {
            for (var parametro : parametros) {
                if ("header".equals(parametro.getIn()) && CSRF_HEADER.equalsIgnoreCase(parametro.getName())) {
                    parametro.required(true).schema(new StringSchema());
                    return;
                }
            }
        }
        operacao.addParametersItem(new HeaderParameter().name(CSRF_HEADER).required(true)
                .description("Valor do cookie XSRF-TOKEN obtido em GET /auth/csrf.")
                .schema(new StringSchema()));
    }

    // Remove o prefixo da versão do começo do caminho, se ele estiver lá
    private static String semPrefixo(String caminho) {
        return caminho.startsWith(ApiPrefixConfiguration.PREFIXO) ? caminho.substring(ApiPrefixConfiguration.PREFIXO.length()) : caminho;
    }
}
