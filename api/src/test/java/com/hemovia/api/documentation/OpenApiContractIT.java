package com.hemovia.api.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("base-entities-test")
class OpenApiContractIT {

    private static final Map<String, Set<String>> EXPECTED_OPERATIONS = Map.ofEntries(
            Map.entry("/auth/csrf", Set.of("get")),
            Map.entry("/auth/login", Set.of("post")),
            Map.entry("/auth/refresh", Set.of("post")),
            Map.entry("/auth/logout", Set.of("post")),
            Map.entry("/auth/me", Set.of("get")),
            Map.entry("/users", Set.of("get", "post")),
            Map.entry("/users/{id}", Set.of("get", "put")),
            Map.entry("/users/{id}/role", Set.of("patch")),
            Map.entry("/users/{id}/deactivate", Set.of("patch")),
            Map.entry("/hospitals", Set.of("post")),
            Map.entry("/donations", Set.of("get", "post")),
            Map.entry("/donations/{id}", Set.of("get", "put", "delete")),
            Map.entry("/blood-bags", Set.of("get", "post")),
            Map.entry("/blood-bags/{id}", Set.of("get", "put", "delete")));

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    @DisplayName("@spec:AC-018 OpenAPI descreve as operações após o merge com a API v1")
    void ac018DocumentsCurrentOperationsAndResponses() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode paths = document.path("paths");
        assertEquals("/api/v1", document.path("servers").get(0).path("url").asText());
        assertEquals(EXPECTED_OPERATIONS.keySet(), objectFieldNames(paths));
        Set<String> operationIds = new java.util.HashSet<>();
        for (var entry : EXPECTED_OPERATIONS.entrySet()) {
            assertEquals(entry.getValue(), objectFieldNames(paths.path(entry.getKey())));
            for (String method : entry.getValue()) {
                JsonNode operation = paths.path(entry.getKey()).path(method);
                assertTrue(operationIds.add(operation.path("operationId").asText()), "operationId único");
                assertTrue(objectFieldNames(operation.path("responses")).stream().anyMatch(code -> code.startsWith("2")));
            }
        }
        assertEquals(22, operationIds.size());
        assertFalse(paths.has("/auth/register"));
        assertFalse(paths.has("/error"));
        assertTrue(paths.path("/auth/csrf").path("get").path("responses").has("204"));
        assertTrue(paths.path("/auth/logout").path("post").path("responses").has("204"));
    }

    @Test
    @DisplayName("@spec:AC-019 schemas OpenAPI refletem DTOs, campos opcionais, enums e unidades")
    void ac019DocumentsDtoSchemasAndExamples() throws Exception {
        JsonNode schemas = getOpenApiDocument().path("components").path("schemas");

        JsonNode register = schemas.path("CreateUserRequest");
        assertRequired(register, "name", "email", "cpf", "password");
        assertTrue(register.path("properties").has("role"));
        assertEquals("email", register.path("properties").path("email").path("format").asText());

        JsonNode login = schemas.path("LoginRequest");
        assertRequired(login, "identifier", "password");

        JsonNode userResponse = schemas.path("UserResponse");
        assertTrue(userResponse.path("properties").has("id"));
        assertEquals("uuid", userResponse.path("properties").path("id").path("format").asText());
        assertFalse(userResponse.path("properties").has("password"));
        assertFalse(userResponse.path("properties").has("token"));
        assertFalse(schemas.has("User"), "entidade interna User não deve ser exposta");

        JsonNode hospital = schemas.path("HospitalRequest");
        assertRequired(hospital, "name", "city", "latitude", "longitude", "priority");
        assertEquals(-90, hospital.path("properties").path("latitude").path("minimum").asDouble());
        assertEquals(90, hospital.path("properties").path("latitude").path("maximum").asDouble());
        assertEquals(-180, hospital.path("properties").path("longitude").path("minimum").asDouble());
        assertEquals(180, hospital.path("properties").path("longitude").path("maximum").asDouble());
        assertEquals(1, hospital.path("properties").path("priority").path("minimum").asInt());
        assertEquals(5, hospital.path("properties").path("priority").path("maximum").asInt());

        JsonNode donationRequest = schemas.path("DonationRequest");
        assertRequired(donationRequest, "collectionCenter", "collectionDate");
        assertEquals("date", donationRequest.path("properties").path("collectionDate").path("format").asText());
        assertTrue(donationRequest.path("properties").has("bloodBagId"));
        assertFalse(requiredFields(donationRequest).contains("bloodBagId"));
        assertFalse(requiredFields(donationRequest).contains("donorId"));
        assertTrue(arrayValues(donationRequest.path("properties").path("bloodType").path("enum"))
                .containsAll(Set.of("A", "B", "AB", "O")));

        JsonNode donationResponse = schemas.path("DonationResponse");
        assertEquals("array", donationResponse.path("properties").path("bloodBags").path("type").asText());
        assertEquals("BloodBagResponse",
                refName(donationResponse.path("properties").path("bloodBags").path("items").path("$ref")));
        assertTrue(donationResponse.path("properties").path("bloodBag").path("description").asText().contains("nula"));

        JsonNode bloodBagRequest = schemas.path("BloodBagRequest");
        assertEquals("date-time", bloodBagRequest.path("properties").path("collectionDate").path("format").asText());
        assertEquals("date-time", bloodBagRequest.path("properties").path("expirationDate").path("format").asText());
        assertEquals("POSITIVE", bloodBagRequest.path("properties").path("rhFactor").path("example").asText());
        assertEquals("WHOLE_BLOOD", bloodBagRequest.path("properties").path("component").path("example").asText());
        assertTrue(bloodBagRequest.path("properties").path("expirationDate").path("example").asText()
            .startsWith("2030-"), "exemplo de validade deve ser futuro");
        assertEquals("number", bloodBagRequest.path("properties").path("volumeMl").path("type").asText());
        assertTrue(bloodBagRequest.path("properties").path("volumeMl").path("description").asText().contains("mL"));
        assertTrue(arrayValues(bloodBagRequest.path("properties").path("bloodTypeEnum").path("enum"))
                .containsAll(Set.of("A", "B", "AB", "O")));
        assertTrue(arrayValues(bloodBagRequest.path("properties").path("status").path("enum"))
                .contains("AVAILABLE"));
        assertTrue(schemas.path("BloodBagResponse").path("properties").has("bloodBagId"));
    }

    @Test
    @DisplayName("@spec:AC-020 OpenAPI descreve cookies e preserva a proteção das rotas")
    void ac020DocumentsCookieSecurity() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode scheme = document.path("components").path("securitySchemes").path("cookieAuth");
        assertEquals("apiKey", scheme.path("type").asText());
        assertEquals("cookie", scheme.path("in").asText());
        assertEquals("HEMOVIA_ACCESS", scheme.path("name").asText());
        assertFalse(document.path("components").path("securitySchemes").has("bearerAuth"));
        assertTrue(document.path("security").get(0).has("cookieAuth"));
        for (String path : Set.of("/auth/login", "/auth/refresh", "/auth/csrf")) {
            String method = path.endsWith("csrf") ? "get" : "post";
            assertTrue(document.path("paths").path(path).path(method).path("security").isEmpty());
        }
        assertUnauthorized(send("GET", "/api/v1/auth/me", null));
        assertUnauthorized(send("GET", "/api/v1/auth/me", null, "token-invalido"));
        assertUnauthorized(send("GET", "/api/v1/donations", null));
        assertEquals(204, send("GET", "/api/v1/auth/csrf", null).statusCode());
    }

    @Test
    @DisplayName("@spec:AC-021 README documenta URLs, configuração, autenticação e pré-requisitos")
    void ac021ReadmeDocumentsSwaggerWorkflow() throws Exception {
        Path readmePath = Path.of("README.md");
        if (!Files.exists(readmePath)) readmePath = Path.of("..", "README.md");
        String readme = Files.readString(readmePath);

        assertTrue(readme.contains("http://localhost:8080/swagger-ui.html"));
        assertTrue(readme.contains("http://localhost:8080/v3/api-docs"));
        assertTrue(readme.contains("HEMOVIA_API_DOCS_ENABLED=true"));
        assertTrue(readme.contains("HEMOVIA_API_DOCS_ENABLED=false"));
        assertTrue(readme.contains("/auth/login"));
        assertTrue(readme.contains("Authorize"));
        assertTrue(readme.contains("JDK 21"));
        assertTrue(readme.contains("PostgreSQL 16"));
        assertTrue(readme.contains("Flyway"));
        assertTrue(readme.contains("Dados legados incompatíveis"));
    }

    private JsonNode getOpenApiDocument() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return objectMapper.readTree(response.body());
    }

    private HttpResponse<String> send(String method, String path, Object body) throws Exception {
        return send(method, path, body, null);
    }

    private HttpResponse<String> send(String method, String path, Object body, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static Set<String> objectFieldNames(JsonNode node) {
        java.util.Set<String> names = new java.util.HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static Set<String> arrayValues(JsonNode node) {
        java.util.Set<String> values = new java.util.HashSet<>();
        node.elements().forEachRemaining(value -> values.add(value.asText()));
        return values;
    }

    private static Set<String> requiredFields(JsonNode schema) {
        java.util.Set<String> required = new java.util.HashSet<>();
        schema.path("required").elements().forEachRemaining(field -> required.add(field.asText()));
        return required;
    }

    private static void assertRequired(JsonNode schema, String... fields) {
        assertTrue(schema.has("properties"), "schema ausente");
        Set<String> required = requiredFields(schema);
        for (String field : fields) {
            assertTrue(required.contains(field), field + " deve ser obrigatório");
        }
    }

    private static void assertPasswordInput(JsonNode password) {
        assertEquals("password", password.path("format").asText());
        assertTrue(password.path("writeOnly").asBoolean());
        assertFalse(password.path("example").asText().isBlank());
        assertFalse(password.path("example").asText().matches("(?i).*(eyJ[a-zA-Z0-9_-]+\\.).*"));
    }

    private static String refName(JsonNode ref) {
        String reference = ref.asText();
        return reference.substring(reference.lastIndexOf('/') + 1);
    }

    private static boolean hasPathIdParameter(JsonNode operation) {
        for (JsonNode parameter : operation.path("parameters")) {
            if ("id".equals(parameter.path("name").asText())
                    && "path".equals(parameter.path("in").asText())
                    && parameter.path("required").asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSecurityRequirement(JsonNode operation, String schemeName) {
        for (JsonNode requirement : operation.path("security")) {
            if (requirement.has(schemeName)) return true;
        }
        return false;
    }

    private static boolean containsParameterNamed(JsonNode operation, String parameterName) {
        for (JsonNode parameter : operation.path("parameters")) {
            if (parameterName.equalsIgnoreCase(parameter.path("name").asText())) return true;
        }
        return false;
    }

    private static void assertUnauthorized(HttpResponse<String> response) {
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "JWT ausente ou inválido deve ser rejeitado; status=" + response.statusCode());
    }
}