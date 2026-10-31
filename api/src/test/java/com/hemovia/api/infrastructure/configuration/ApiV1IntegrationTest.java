package com.hemovia.api.infrastructure.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Ciclo 12 da Seção 1: o OpenAPI documenta a API e o Swagger UI abre.
Sobe a aplicação inteira com o filtro de segurança real, por isso precisa do PostgreSQL.
*/
@Tag("integration")
@SpringBootTest
class ApiV1IntegrationTest {

    // Contexto completo da aplicação, entregue pelo Spring
    @Autowired
    private WebApplicationContext contexto;

    // Cliente HTTP de mentira com a cadeia de segurança aplicada, como numa chamada real
    private MockMvc mvc;

    @BeforeEach
    void prepararMvc() {
        // Aplicamos o springSecurity() para a segurança também ser testada
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    @Test
    void deveExporOpenApiComServidorApiV1() throws Exception {
        // Quando pedimos a documentação, então ela existe e o único servidor é o /api/v1
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servers[0].url").value("/api/v1"));
    }

    @Test
    void deveListarOsCaminhosSemRepetirOPrefixoDoServidor() throws Exception {
        // O servidor já carrega /api/v1, então os caminhos aparecem sem ele (senão o "Try it out" duplicaria o prefixo)
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/donations']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/donations']").doesNotExist());
    }

    @Test
    void deveAbrirOSwaggerUi() throws Exception {
        // Quando abrimos a página do Swagger UI, então ela responde
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void deveManterMeProtegidaSemTokenNoNovoPrefixo() throws Exception {
        // Caracterização: sem credencial, a rota de sessão continua barrada (a Seção 3 troca isso por 401 com cookies)
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().is4xxClientError());
    }

    @Test
    void deveDeixarDeExistirARotaAntigaSemPrefixo() throws Exception {
        // Os caminhos antigos sem /api/v1 deixam de existir
        mvc.perform(get("/donations")).andExpect(status().isNotFound());
    }
}
