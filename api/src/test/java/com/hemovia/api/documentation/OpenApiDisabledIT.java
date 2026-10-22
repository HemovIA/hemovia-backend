package com.hemovia.api.documentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "HEMOVIA_API_DOCS_ENABLED=false")
@ActiveProfiles("base-entities-test")
class OpenApiDisabledIT {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Test
    @DisplayName("@spec:AC-017 HEMOVIA_API_DOCS_ENABLED desabilita documento, configura\u00e7\u00e3o e interface")
    void ac017_doesNotExposeDocumentationWhenDisabled() throws Exception {
        for (String path : Stream.of("/v3/api-docs", "/v3/api-docs/swagger-config", "/swagger-ui.html",
                "/swagger-ui/index.html", "/swagger-ui/swagger-initializer.js").toList()) {
            HttpResponse<String> response = get(path);
            assertEquals(404, response.statusCode(),
                    () -> path + " n\u00e3o deve disponibilizar documenta\u00e7\u00e3o quando desabilitado; status=" + response.statusCode());
        }
        assertEquals(200, get("/donations").statusCode(), "A API de negocio continua acessivel");
        int protectedStatus = get("/auth/me").statusCode();
        assertTrue(protectedStatus == 401 || protectedStatus == 403, "A protecao existente permanece ativa");
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
