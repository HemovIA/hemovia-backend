package com.hemovia.api.documentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "HEMOVIA_API_DOCS_ENABLED=false")
class OpenApiDisabledIT {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Test
    @DisplayName("@spec:AC-017 HEMOVIA_API_DOCS_ENABLED desabilita documento, configura\u00e7\u00e3o e interface")
    void ac017_doesNotExposeDocumentationWhenDisabled() throws Exception {
        for (String path : Stream.of("/v3/api-docs", "/v3/api-docs/swagger-config", "/swagger-ui.html").toList()) {
            HttpResponse<String> response = get(path);
            assertTrue(response.statusCode() >= 400,
                    () -> path + " n\u00e3o deve disponibilizar documenta\u00e7\u00e3o quando desabilitado; status=" + response.statusCode());
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
