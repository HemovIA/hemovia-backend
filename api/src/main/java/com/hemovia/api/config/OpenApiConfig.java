package com.hemovia.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hemoviaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hemovia API")
                        .version("v1")
                        .description("Documentação da API Hemovia para gestão de doações e bolsas de sangue."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .description("Informe somente o token JWT obtido em /auth/login.")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
