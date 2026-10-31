package com.hemovia.api.acceptance;

import com.hemovia.api.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Aceitação — POOUS03 Gestão de Cadastro de Hospitais.
 * Cenários em src/test/resources/features/poous03_cadastro_hospital.feature
 */
@Tag("integration")
@SpringBootTest
class HospitalAcceptanceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private HospitalRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("Dado dados válidos, quando cadastro o hospital, então ele é salvo e recebo 201")
    void cadastroValido() throws Exception {
        long antes = repository.count();

        mockMvc.perform(post("/api/v1/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hospital Sintético Norte","city":"Recife",
                                 "latitude":-8.05,"longitude":-34.90,"priority":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.city").value("Recife"));

        assertThat(repository.count()).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("Dado latitude fora de -90..90, quando cadastro o hospital, então recebo 400 e nada é salvo")
    void coordenadaInvalida() throws Exception {
        long antes = repository.count();

        mockMvc.perform(post("/api/v1/hospitals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Hospital Fora do Mapa","city":"Recife",
                                 "latitude":95.0,"longitude":-34.90,"priority":2}
                                """))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isEqualTo(antes);
    }
}
