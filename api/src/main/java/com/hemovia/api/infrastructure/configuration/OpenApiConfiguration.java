package com.hemovia.api.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
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

    @Bean
    public OpenAPI hemoviaOpenApi() {
        // Título e versão aparecem no topo do Swagger UI; o servidor carrega o prefixo /api/v1
        return new OpenAPI()
                .info(new Info().title("HEMOVIA API").version("v1")
                        .description("Gestão e distribuição de hemocomponentes. Todos os dados são sintéticos."))
                .servers(List.of(new Server().url(ApiPrefixConfiguration.PREFIXO)));
    }

    @Bean
    public OpenApiCustomizer caminhosRelativosAoServidor() {
        // O springdoc lê os caminhos já com /api/v1; como o servidor também o traz, tiramos do caminho para o "Try it out" não duplicar
        return openApi -> {
            Paths relativos = new Paths();
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((caminho, item) -> relativos.addPathItem(semPrefixo(caminho), item));
            }
            openApi.setPaths(relativos);
        };
    }

    // Remove o prefixo da versão do começo do caminho, se ele estiver lá
    private static String semPrefixo(String caminho) {
        return caminho.startsWith(ApiPrefixConfiguration.PREFIXO) ? caminho.substring(ApiPrefixConfiguration.PREFIXO.length()) : caminho;
    }
}
