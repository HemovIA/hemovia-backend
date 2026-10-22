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

    private static final Map<String, Map<String, String>> EXPECTED_OPERATIONS = Map.of(
            "/auth/register", Map.of("post", "201"),
            "/auth/login", Map.of("post", "200"),
            "/auth/me", Map.of("get", "200"),
            "/hospitals", Map.of("post", "201"),
            "/donations", Map.of("get", "200", "post", "201"),
            "/donations/{id}", Map.of("get", "200", "put", "200", "delete", "204"),
            "/blood-bags", Map.of("get", "200", "post", "201"),
            "/blood-bags/{id}", Map.of("get", "200", "put", "200", "delete", "204"));

    private static final Set<String> NOT_FOUND_ERRORS = Set.of("400", "404");

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    @DisplayName("@spec:AC-018 OpenAPI descreve as 14 operações e suas respostas HTTP reais")
    void ac018DocumentsCurrentOperationsAndResponses() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode paths = document.path("paths");

        assertEquals(EXPECTED_OPERATIONS.keySet(), objectFieldNames(paths));

        int operationCount = 0;
        for (Map.Entry<String, Map<String, String>> pathEntry : EXPECTED_OPERATIONS.entrySet()) {
            JsonNode path = paths.path(pathEntry.getKey());
            assertEquals(pathEntry.getValue().keySet(), objectFieldNames(path));

            for (Map.Entry<String, String> operationEntry : pathEntry.getValue().entrySet()) {
                JsonNode operation = path.path(operationEntry.getKey());
                assertTrue(operation.hasNonNull("operationId"), pathEntry.getKey());
                assertFalse(operation.path("operationId").asText().isBlank());
                assertTrue(operation.hasNonNull("summary"), pathEntry.getKey());
                assertFalse(operation.path("summary").asText().isBlank());
                assertTrue(operation.path("tags").isArray() && !operation.path("tags").isEmpty());
                assertEquals(expectedTag(pathEntry.getKey()), operation.path("tags").get(0).asText());

                boolean acceptsBody = Set.of("post", "put").contains(operationEntry.getKey());
                assertEquals(acceptsBody, operation.has("requestBody"), pathEntry.getKey());
                if (acceptsBody) {
                    assertTrue(operation.path("requestBody").path("content").has("application/json"));
                }

                JsonNode responses = operation.path("responses");
                assertTrue(responses.has(operationEntry.getValue()), pathEntry.getKey());
                if (!"204".equals(operationEntry.getValue())) {
                    assertTrue(responses.path(operationEntry.getValue()).has("content"), pathEntry.getKey());
                }
                for (String errorCode : expectedErrors(pathEntry.getKey(), operationEntry.getKey())) {
                    assertTrue(responses.has(errorCode), pathEntry.getKey() + " " + errorCode);
                    assertFalse(responses.path(errorCode).path("description").asText().isBlank());
                }
                if ("204".equals(operationEntry.getValue())) {
                    assertFalse(responses.path("204").has("content"), "204 não deve anunciar corpo");
                }
                if (pathEntry.getKey().endsWith("/{id}")) {
                    assertTrue(hasPathIdParameter(operation), pathEntry.getKey());
                }
                operationCount++;
            }
        }

        assertEquals(14, operationCount);
        Set<String> operationIds = EXPECTED_OPERATIONS.entrySet().stream()
                .flatMap(path -> path.getValue().keySet().stream()
                        .map(method -> paths.path(path.getKey()).path(method).path("operationId").asText()))
                .collect(Collectors.toSet());
        assertEquals(14, operationIds.size(), "operationId deve ser único");
        assertFalse(paths.has("/error"));
    }

    @Test
    @DisplayName("@spec:AC-019 schemas OpenAPI refletem DTOs, campos opcionais, enums e unidades")
    void ac019DocumentsDtoSchemasAndExamples() throws Exception {
        JsonNode schemas = getOpenApiDocument().path("components").path("schemas");

        JsonNode register = schemas.path("RegisterRequest");
        assertRequired(register, "name", "email", "cpf", "password");
        assertPasswordInput(register.path("properties").path("password"));
        assertEquals("email", register.path("properties").path("email").path("format").asText());

        JsonNode login = schemas.path("LoginRequest");
        assertRequired(login, "password");
        assertPasswordInput(login.path("properties").path("password"));
        assertEquals("ana.souza@example.test", login.path("properties").path("email").path("example").asText());

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
    @DisplayName("@spec:AC-020 JWT é documentado apenas nas operações protegidas")
    void ac020DocumentsBearerSecurityWithoutExposingInternalParameters() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode bearer = document.path("components").path("securitySchemes").path("bearerAuth");

        assertEquals("http", bearer.path("type").asText());
        assertEquals("bearer", bearer.path("scheme").asText());
        assertEquals("JWT", bearer.path("bearerFormat").asText());

        JsonNode operations = document.path("paths");
        JsonNode authenticatedMe = operations.path("/auth/me").path("get");
        assertTrue(hasSecurityRequirement(authenticatedMe, "bearerAuth"));
        assertFalse(containsParameterNamed(authenticatedMe, "user"));
        assertFalse(hasSecurityRequirement(operations.path("/auth/register").path("post"), "bearerAuth"));
        assertFalse(hasSecurityRequirement(operations.path("/auth/login").path("post"), "bearerAuth"));

        for (Map.Entry<String, Map<String, String>> pathEntry : EXPECTED_OPERATIONS.entrySet()) {
            for (String method : pathEntry.getValue().keySet()) {
                if (!"/auth/me".equals(pathEntry.getKey())) {
                    assertFalse(hasSecurityRequirement(operations.path(pathEntry.getKey()).path(method), "bearerAuth"),
                            pathEntry.getKey() + " " + method + " continua público");
                }
            }
        }
    }

    @Test
    @DisplayName("@spec:AC-020 chamadas HTTP preservam JWT válido, ausente e inválido")
    void ac020PreservesHttpAuthenticationBehavior() throws Exception {
        String email = "swagger-" + java.util.UUID.randomUUID() + "@example.test";
        String cpf = "9" + String.format("%013d", Math.floorMod(System.nanoTime(), 1_000_000_000_000L));
        String password = "senha-teste-2026";

        HttpResponse<String> registration = send("POST", "/auth/register", Map.of(
                "name", "Usuário Sintético Swagger",
                "email", email,
                "cpf", cpf,
                "password", password));
        assertEquals(201, registration.statusCode());

        assertUnauthorized(send("GET", "/auth/me", null));
        assertUnauthorized(send("GET", "/auth/me", null, "token-invalido"));

        HttpResponse<String> login = send("POST", "/auth/login", Map.of("email", email, "password", password));
        assertEquals(200, login.statusCode());
        String token = objectMapper.readTree(login.body()).path("token").asText();
        assertFalse(token.isBlank());

        HttpResponse<String> authenticatedUser = send("GET", "/auth/me", null, token);
        assertEquals(200, authenticatedUser.statusCode());
        assertEquals(email, objectMapper.readTree(authenticatedUser.body()).path("email").asText());
        assertEquals(200, send("GET", "/donations", null).statusCode(),
                "operações que hoje são públicas continuam acessíveis sem JWT");
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

    private static Set<String> expectedErrors(String path, String method) {
        if ("/auth/register".equals(path)) return Set.of("400");
        if ("/auth/login".equals(path)) return Set.of("400", "401");
        if ("/auth/me".equals(path)) return Set.of("401", "403");
        if ("/hospitals".equals(path)) return Set.of("400");
        if (path.endsWith("/{id}") || ("post".equals(method) && !"/hospitals".equals(path))) {
            return NOT_FOUND_ERRORS;
        }
        return Set.of();
    }

    private static String expectedTag(String path) {
        if (path.startsWith("/auth")) return "Autenticação";
        if (path.startsWith("/hospitals")) return "Hospitais";
        if (path.startsWith("/donations")) return "Doações";
        return "Bolsas de sangue";
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