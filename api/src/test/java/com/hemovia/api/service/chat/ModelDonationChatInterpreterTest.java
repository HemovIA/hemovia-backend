package com.hemovia.api.service.chat;

import com.hemovia.api.dto.request.DonationChatRequest.Message;
import com.hemovia.api.dto.response.DonationChatResponse.Source;
import com.hemovia.api.model.exception.DonationChatUnavailableException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ModelDonationChatInterpreterTest {

    private static final JsonMapper JSON = new JsonMapper();
    private static final List<Message> HISTORY = List.of(
            new Message("user", "Fiz uma tatuagem."),
            new Message("assistant", "Quando você fez a tatuagem?"),
            new Message("user", "Há três meses."));
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> providerResponse = new AtomicReference<>();
    private final AtomicInteger providerStatus = new AtomicInteger(200);
    private HttpServer provider;
    private DonationChatCatalog catalog;
    private ModelDonationChatInterpreter interpreter;

    @BeforeEach
    void startFakeProvider() throws IOException {
        catalog = DonationChatCatalogTest.catalog();
        provider = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        provider.createContext("/api/chat", this::reply);
        provider.start();
        URI endpoint = URI.create("http://127.0.0.1:" + provider.getAddress().getPort() + "/api/chat");
        interpreter = new ModelDonationChatInterpreter(catalog, endpoint, "synthetic-model",
                Duration.ofSeconds(2), Duration.ofSeconds(3));
    }

    @AfterEach
    void stopFakeProvider() {
        if (provider != null) {
            provider.stop(0);
        }
    }

    @Test
    @DisplayName("@spec:AC-138 Ollama recebe seleção estruturada e somente os turnos enviados")
    void sendsOnlyBoundedConversationToLocalProvider() {
        providerResponse.set(response("{\"cards\":[\"tatuagem-tres-meses\"]}"));

        assertThat(interpreter.selectCards(HISTORY)).containsExactly("tatuagem-tres-meses");
        JsonNode request = JSON.readTree(requestBody.get());
        assertThat(request.propertyNames()).containsExactlyInAnyOrder("model", "messages", "stream", "format", "options");
        assertThat(request.path("model").asText()).isEqualTo("synthetic-model");
        assertThat(request.path("stream").asBoolean()).isFalse();
        assertThat(request.path("format").path("additionalProperties").asBoolean(true)).isFalse();
        JsonNode messages = request.path("messages");
        assertThat(messages.size()).isEqualTo(4);
        assertThat(messages.path(0).path("role").asText()).isEqualTo("system");
        for (int index = 0; index < HISTORY.size(); index++) {
            JsonNode message = messages.get(index + 1);
            assertThat(message.propertyNames()).containsExactlyInAnyOrder("role", "content");
            assertThat(message.path("role").asText()).isEqualTo(HISTORY.get(index).role());
            assertThat(message.path("content").asText()).isEqualTo(HISTORY.get(index).content());
        }
    }

    @Test
    @DisplayName("@spec:AC-139 seleção desconhecida, texto livre e referências do modelo vão à triagem")
    void malformedOrUnknownSelectionReturnsOnlyOfficialReferral() {
        List<String> invalid = List.of(
                "Você está apto. Visite https://example.test",
                "{}",
                "{\"cards\":[\"tatuagem-tres-meses\",\"id-inventado\"]}",
                "{\"cards\":[\"tatuagem-tres-meses\"],\"sources\":[\"https://example.test\"]}",
                "{\"cards\":[{\"id\":\"tatuagem-tres-meses\"}]}",
                "{\"cards\":[\"esclarecimento\",\"tatuagem\"]}",
                "{\"cards\":[\"tatuagem\"],\"cards\":[\"dengue\"]}",
                "{\"cards\":[\"tatuagem\"]} {\"cards\":[\"dengue\"]}");

        for (String content : invalid) {
            providerResponse.set(response(content));
            var safe = catalog.respond(interpreter.selectCards(HISTORY));
            assertThat(safe.answer()).contains("procure um hemocentro").doesNotContain("example.test");
            assertThat(safe.sources()).extracting(Source::url)
                    .containsExactly(DonationChatCatalogTest.FAQ, DonationChatCatalogTest.BLOOD_CENTERS);
        }
    }

    @Test
    @DisplayName("@spec:AC-134 indisponibilidade do Ollama oculta detalhes do provedor")
    void reportsProviderFailureWithoutLeakingBody() {
        providerStatus.set(503);
        providerResponse.set("DETALHE-SINTÉTICO-DO-PROVEDOR");

        assertThatExceptionOfType(DonationChatUnavailableException.class)
                .isThrownBy(() -> interpreter.selectCards(HISTORY))
                .withMessage("O assistente está temporariamente indisponível. Tente novamente mais tarde.")
                .withNoCause();
    }

    private void reply(HttpExchange exchange) throws IOException {
        try (exchange) {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = providerResponse.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(providerStatus.get(), body.length);
            exchange.getResponseBody().write(body);
        }
    }

    private static String response(String selection) {
        return JSON.writeValueAsString(Map.of("message", Map.of("content", selection)));
    }
}

