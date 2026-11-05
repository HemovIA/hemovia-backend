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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("base-entities-test")
class OpenApiAvailabilityIT {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Test
    @DisplayName("@spec:AC-016 Swagger UI e OpenAPI s\u00e3o disponibilizados por HTTP quando habilitados")
    void ac016_exposesGeneratedDocumentationAndUiConfiguration() throws Exception {
        HttpResponse<String> document = get("/v3/api-docs");

        assertEquals(200, document.statusCode());
        assertTrue(document.headers().firstValue("content-type").orElse("").contains("application/json"));
        assertTrue(document.body().contains("\"title\":\"Hemovia API\""));
        assertTrue(document.body().contains("\"version\":\"v1\""));
        assertTrue(document.body().contains("Documenta\u00e7\u00e3o da API Hemovia"));
        assertFalse(document.body().toLowerCase().contains("petstore"));

        HttpResponse<String> configuration = get("/v3/api-docs/swagger-config");
        assertEquals(200, configuration.statusCode());
        assertTrue(configuration.body().contains("/v3/api-docs"));
        assertFalse(configuration.body().toLowerCase().contains("petstore"));

        HttpResponse<String> ui = get("/swagger-ui.html");
        assertEquals(200, ui.statusCode(), "O redirect precisa terminar na interface carregada");
        assertTrue(ui.headers().firstValue("content-type").orElse("").contains("text/html"));
        assertTrue(ui.body().contains("id=\"swagger-ui\""));
        assertTrue(ui.body().contains("swagger-ui-bundle.js"));

        HttpResponse<String> initializer = get("/swagger-ui/swagger-initializer.js");
        assertEquals(200, initializer.statusCode());
        assertTrue(initializer.body().contains("SwaggerUIBundle"));
        assertFalse(initializer.body().toLowerCase().contains("petstore"));
        HttpResponse<String> bundle = get("/swagger-ui/swagger-ui-bundle.js");
        assertEquals(200, bundle.statusCode());
        assertTrue(bundle.body().contains("SwaggerUIBundle"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
