package com.hemovia.api.acceptance;

import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.repository.BloodBagRepository;
import com.jayway.jsonpath.JsonPath;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Aceitação — POOUS01 (Cadastro de Doação e Entrada de Bolsa no Estoque)
 * e POOUS02 (Controle de Validade das Bolsas).
 * Cenários em src/test/resources/features/
 */
@Tag("integration")
@SpringBootTest
class DonationAcceptanceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private BloodBagRepository bloodBagRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String doacao(String centro, LocalDate validade) {
        String campoCentro = centro == null ? "" : "\"collectionCenter\":\"" + centro + "\",";
        return """
                {%s"collectionDate":"%s","bloodType":"O","rhFactor":"-",
                 "component":"Concentrado de Hemácias","expirationDate":"%s"}
                """.formatted(campoCentro, LocalDate.now(), validade);
    }

    @Test
    @DisplayName("POOUS01 — Dado uma doação válida, quando registro, então a bolsa entra no estoque como AVAILABLE")
    void doacaoValidaGeraBolsaDisponivel() throws Exception {
        String body = mockMvc.perform(post("/api/v1/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doacao("Hemocentro Sintético Recife", LocalDate.now().plusDays(35))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID bagId = UUID.fromString(JsonPath.read(body, "$.bloodBag.bloodBagId"));
        BloodBag bag = bloodBagRepository.findById(bagId).orElseThrow();
        assertThat(bag.getStatus()).isEqualTo(BloodBagStatus.AVAILABLE);
    }

    @Test
    @DisplayName("POOUS01 — Dado uma doação sem centro de coleta, quando registro, então recebo 400")
    void doacaoIncompletaRetorna400() throws Exception {
        long antes = bloodBagRepository.count();

        mockMvc.perform(post("/api/v1/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doacao(null, LocalDate.now().plusDays(35))))
                .andExpect(status().isBadRequest());

        assertThat(bloodBagRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("POOUS02 — Dado uma bolsa com validade vencida, quando registro, então recebo 400 e nada entra no estoque")
    void bolsaVencidaERejeitada() throws Exception {
        long antes = bloodBagRepository.count();

        mockMvc.perform(post("/api/v1/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doacao("Hemocentro Sintético Recife", LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest());

        assertThat(bloodBagRepository.count()).isEqualTo(antes);
    }
}
