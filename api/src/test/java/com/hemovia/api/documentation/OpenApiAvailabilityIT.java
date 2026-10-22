package com.hemovia.api.documentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiAvailabilityIT {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
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
        assertTrue(ui.statusCode() == 200 || ui.statusCode() == 302 || ui.statusCode() == 307,
                "A entrada da Swagger UI deve responder ou redirecionar para seus recursos");
        if (ui.statusCode() != 200) {
            assertTrue(ui.headers().firstValue("location").orElse("").contains("swagger-ui"));
        }

        HttpResponse<String> initializer = get("/swagger-ui/swagger-initializer.js");
        assertEquals(200, initializer.statusCode());
        assertTrue(initializer.body().contains("SwaggerUIBundle"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
