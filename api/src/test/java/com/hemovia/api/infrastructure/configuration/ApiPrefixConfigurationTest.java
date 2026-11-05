package com.hemovia.api.infrastructure.configuration;

import br.exemplo.externo.ControllerExterno;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Ciclo 13 da Seção 1: toda a API passa a viver sob /api/v1 (decisão D2).
Sobe só o Spring MVC com a configuração do prefixo, sem banco e sem segurança.
*/
@SpringJUnitWebConfig(classes = ApiPrefixConfigurationTest.Config.class)
class ApiPrefixConfigurationTest {

    // Configuração mínima de MVC com a nossa configuração de prefixo e dois controllers de exemplo
    @Configuration
    @EnableWebMvc
    @Import(ApiPrefixConfiguration.class)
    static class Config {

        @Bean
        ControllerDaApi controllerDaApi() {
            // Controller dentro do pacote com.hemovia.api, que deve receber o prefixo
            return new ControllerDaApi();
        }

        @Bean
        ControllerExterno controllerExterno() {
            // Controller de fora do pacote da API, que não deve receber o prefixo
            return new ControllerExterno();
        }
    }

    // Controller de exemplo da nossa API: só declara /ping, o prefixo vem da configuração
    @RestController
    static class ControllerDaApi {

        @GetMapping("/ping")
        String ping() {
            return "pong";
        }
    }

    // Cliente HTTP de mentira ligado ao contexto de teste
    private final MockMvc mvc;

    ApiPrefixConfigurationTest(WebApplicationContext contexto) {
        // Montamos o MockMvc a partir do contexto que o Spring criou para o teste
        this.mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void deveResponderSobApiV1QuandoOControllerEstaNoPacoteDaApi() throws Exception {
        // Quando chamamos o caminho com prefixo, então o controller responde
        mvc.perform(get("/api/v1/ping")).andExpect(status().isOk()).andExpect(content().string("pong"));
    }

    @Test
    void deveDeixarDeResponderNoCaminhoSemPrefixo() throws Exception {
        // Quando chamamos o caminho antigo, sem prefixo, então ele não existe mais
        mvc.perform(get("/ping")).andExpect(status().isNotFound());
    }

    @Test
    void naoDevePrefixarControllersDeOutrosPacotes() throws Exception {
        // Controllers de bibliotecas, como o do springdoc, continuam nos caminhos originais
        mvc.perform(get("/externo")).andExpect(status().isOk());
    }
}
