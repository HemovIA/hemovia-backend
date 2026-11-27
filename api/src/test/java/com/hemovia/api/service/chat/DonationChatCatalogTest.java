package com.hemovia.api.service.chat;

import com.hemovia.api.dto.response.DonationChatResponse;
import com.hemovia.api.dto.response.DonationChatResponse.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DonationChatCatalogTest {

    static final String ORDINANCE = "https://bvsms.saude.gov.br/bvs/saudelegis/gm/2026/prt11685_03_07_2026.html";
    static final String FAQ = "https://www.gov.br/saude/pt-br/composicao/saes/doacao-de-sangue/faq";
    static final String BLOOD_CENTERS =
            "https://www.gov.br/saude/pt-br/composicao/saes/doacao-de-sangue/arquivos/lista-de-hemocentros.pdf/view";

    @Test
    @DisplayName("@spec:AC-135 três meses entre os dois prazos normativos da tatuagem")
    void explainsTattooAfterThreeMonths() throws IOException {
        DonationChatResponse response = catalog().respond(List.of("tatuagem-tres-meses"));

        assertThat(response.answer()).contains(
                "sete dias quando o hemocentro consegue avaliar a segurança",
                "quatro meses quando essa avaliação é inviável",
                "a de quatro meses ainda não foi cumprida",
                "Confirme com o hemocentro");
        assertThat(response.sources()).extracting(Source::url).containsExactly(ORDINANCE, BLOOD_CENTERS);
    }

    @Test
    @DisplayName("@spec:AC-136 dúvida fora do catálogo é encaminhada à triagem com links oficiais")
    void refersUncoveredOrIndividualCasesToScreening() throws IOException {
        DonationChatCatalog catalog = catalog();

        assertThat(catalog.respond(List.of("encaminhamento")).answer())
                .contains("procure um hemocentro", "equipe avalia o seu caso durante a triagem");
        assertThat(catalog.respond(List.of("encaminhamento")).sources()).extracting(Source::url)
                .containsExactly(FAQ, BLOOD_CENTERS);
        assertThat(catalog.respond(List.of("esclarecimento")).answer()).contains("Você pode esclarecer");
        assertThat(catalog.respond(List.of("esclarecimento")).sources()).extracting(Source::url)
                .containsExactly(FAQ);
    }

    @Test
    @DisplayName("@spec:AC-137 catálogo cobre os temas iniciais com a referência correspondente")
    void coversInitialDonationTopics() throws IOException {
        DonationChatCatalog catalog = catalog();
        Map<String, List<String>> expected = Map.ofEntries(
                Map.entry("requisitos", List.of("50 kg", ORDINANCE)),
                Map.entry("intervalos", List.of("dois meses", ORDINANCE)),
                Map.entry("preparacao", List.of("documento oficial com foto", FAQ)),
                Map.entry("tatuagem", List.of("sete dias", ORDINANCE)),
                Map.entry("vacinas", List.of("48 horas", ORDINANCE)),
                Map.entry("procedimentos", List.of("depende do tipo de procedimento", ORDINANCE)),
                Map.entry("medicamentos", List.of("mantenha o tratamento conforme prescrito", ORDINANCE)),
                Map.entry("sintomas", List.of("adie a doação", FAQ)),
                Map.entry("dengue", List.of("seis meses para dengue grave", FAQ)),
                Map.entry("onde-doar", List.of("lista nacional", BLOOD_CENTERS)),
                Map.entry("piercing", List.of("quatro meses após a retirada", ORDINANCE)),
                Map.entry("gestacao", List.of("12 semanas após o parto ou abortamento", ORDINANCE)),
                Map.entry("menstruacao", List.of("A menstruação por si só", FAQ)),
                Map.entry("transfusao", List.of("quatro meses a partir da data da transfusão", FAQ)));
        expected.forEach((id, known) -> {
            DonationChatResponse response = catalog.respond(List.of(id));
            assertThat(response.answer()).as(id).contains(known.getFirst());
            assertThat(response.sources()).as(id).extracting(Source::url).contains(known.getLast());
        });
        assertThat(catalog.respond(List.of("requisitos", "preparacao")).sources()).extracting(Source::url)
                .containsExactly(ORDINANCE, FAQ);
    }

    static DonationChatCatalog catalog() throws IOException {
        try (InputStream input = new ClassPathResource("donation-chat/catalog.json").getInputStream()) {
            return new DonationChatCatalog(input);
        }
    }
}

