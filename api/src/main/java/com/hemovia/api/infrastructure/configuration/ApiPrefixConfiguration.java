package com.hemovia.api.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
Coloca toda a nossa API sob /api/v1 sem repetir o prefixo em cada controller (decisão D2).
*/
@Configuration
public class ApiPrefixConfiguration implements WebMvcConfigurer {

    // Prefixo de versão da API; o OpenAPI usa a mesma constante como servidor
    public static final String PREFIXO = "/api/v1";

    // Pacote onde vivem os nossos controllers; controllers de bibliotecas ficam de fora
    private static final String PACOTE_DA_API = "com.hemovia.api";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurador) {
        // Só recebe o prefixo o @RestController do nosso pacote, nunca o do springdoc nem o do actuator
        configurador.addPathPrefix(PREFIXO,
                HandlerTypePredicate.forBasePackage(PACOTE_DA_API).and(HandlerTypePredicate.forAnnotation(RestController.class)));
    }
}
