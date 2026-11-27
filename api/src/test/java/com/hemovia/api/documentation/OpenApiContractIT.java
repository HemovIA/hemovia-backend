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
            Map.entry("/auth/register", Set.of("post")),
            Map.entry("/auth/register/confirm", Set.of("post")),
            Map.entry("/auth/refresh", Set.of("post")),
            Map.entry("/auth/logout", Set.of("post")),
            Map.entry("/auth/me", Set.of("get")),
            Map.entry("/users", Set.of("get", "post")),
            Map.entry("/users/{id}", Set.of("get", "put")),
            Map.entry("/users/{id}/role", Set.of("patch")),
            Map.entry("/users/{id}/deactivate", Set.of("patch")),
            Map.entry("/hospitals", Set.of("get", "post")),
            Map.entry("/donors", Set.of("get", "post")),
            Map.entry("/hospitals/{id}", Set.of("get")),
            Map.entry("/blood-centers", Set.of("get", "post")),
            Map.entry("/blood-centers/{id}", Set.of("get", "put", "delete")),
            Map.entry("/requests", Set.of("get", "post")),
            Map.entry("/requests/{id}", Set.of("get")),
            Map.entry("/requests/{id}/allocate", Set.of("post")),
            Map.entry("/requests/{id}/cancel", Set.of("post")),
            Map.entry("/donation-chat/messages", Set.of("post")),
            Map.entry("/donations", Set.of("get", "post")),
            Map.entry("/donations/{id}", Set.of("get", "put", "delete")),
            Map.entry("/blood-bags", Set.of("get", "post")),
            Map.entry("/blood-bags/{id}", Set.of("get", "put", "delete")),
            Map.entry("/route-graph/nodes", Set.of("get", "post")),
            Map.entry("/route-graph/edges", Set.of("get", "post")),
            Map.entry("/routes/calculate", Set.of("post")),
            Map.entry("/vehicles", Set.of("get", "post")),
            Map.entry("/transports", Set.of("get")),
            Map.entry("/transports/{id}", Set.of("get")),
            Map.entry("/transports/start", Set.of("post")),
            Map.entry("/transports/{id}/complete", Set.of("post")),
            Map.entry("/statistics/inventory", Set.of("get")),
            Map.entry("/statistics/demand", Set.of("get")));

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
        assertEquals(52, operationIds.size());
        assertTrue(paths.path("/auth/register").path("post").path("responses").has("202"));
        assertTrue(paths.path("/auth/register/confirm").path("post").path("responses").has("201"));
        assertFalse(paths.has("/error"));
        assertTrue(paths.path("/auth/csrf").path("get").path("responses").has("204"));
        assertTrue(paths.path("/auth/logout").path("post").path("responses").has("204"));
        JsonNode chat = paths.path("/donation-chat/messages").path("post");
        assertTrue(chat.path("security").get(0).has("cookieAuth"));
        assertTrue(chat.path("requestBody").path("content").path("application/json").path("schema").has("$ref"));
        assertTrue(objectFieldNames(chat.path("responses")).containsAll(Set.of("200", "400", "401", "403", "503")));
    }

    @Test @DisplayName("@spec:AC-092 OpenAPI descreve consultas, filtros, autenticação e erros")
    void consultasOperacionais() throws Exception {
        JsonNode doc=getOpenApiDocument();
        for (String base : new String[]{"/requests","/transports","/hospitals"}) {
            for (String path : new String[]{base,base+"/{id}"}) {
                JsonNode op=doc.path("paths").path(path).path("get");
                assertTrue(op.path("security").isArray(),path);
                for(String code:new String[]{"200","400","401","403","404"}) assertTrue(op.path("responses").has(code),path+" "+code);
                assertTrue(op.path("responses").path("200").path("content").path("*/*").path("schema").has("$ref"),path);
            }
            JsonNode params=doc.path("paths").path(base).path("get").path("parameters");
            Set<String> names=new java.util.HashSet<>(); params.forEach(v -> names.add(v.path("name").asText()));
            assertTrue(names.containsAll(Set.of("page","size")),base);
            Set<String> filters=switch(base) { case "/requests" -> Set.of("status","hospitalId","bloodType","rhFactor","component","priority"); case "/transports" -> Set.of("status","requestId","vehicleId"); default -> Set.of("name","city"); };
            assertTrue(names.containsAll(filters),base);
        }
        JsonNode schemas=doc.path("components").path("schemas");
        assertTrue(schemas.has("HospitalResponse")); assertTrue(schemas.has("BloodRequestDetailsResponse")); assertTrue(schemas.has("TransportDetailsResponse"));
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
        for (String path : Set.of("/auth/login", "/auth/refresh", "/auth/csrf", "/auth/register", "/auth/register/confirm")) {
            String method = path.endsWith("csrf") ? "get" : "post";
            assertTrue(document.path("paths").path(path).path(method).path("security").isEmpty());
        }
        for (var entry : EXPECTED_OPERATIONS.entrySet()) {
            String path = entry.getKey();
            for (String method : entry.getValue()) {
                boolean requiresCsrf = !method.equals("get") && !path.equals("/auth/login");
                JsonNode operation = document.path("paths").path(path).path(method);
                int csrfHeaders = 0;
                for (JsonNode parameter : operation.path("parameters")) {
                    if ("X-XSRF-TOKEN".equalsIgnoreCase(parameter.path("name").asText())
                            && "header".equals(parameter.path("in").asText())) {
                        csrfHeaders++;
                        assertTrue(parameter.path("required").asBoolean(), method + " " + path);
                        assertEquals("string", parameter.path("schema").path("type").asText(), method + " " + path);
                    }
                }
                assertEquals(requiresCsrf ? 1 : 0, csrfHeaders, method + " " + path);
            }
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

    @Test
    @DisplayName("@spec:AC-025 OpenAPI documenta emissão hospitalar, segurança e respostas")
    void ac025DocumentsHospitalRequestContract() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode operation = document.path("paths").path("/requests").path("post");
        assertFalse(operation.isMissingNode(), "POST /requests deve estar documentado");

        JsonNode requestBody = operation.path("requestBody");
        assertTrue(requestBody.path("required").asBoolean(), "corpo obrigatório");
        JsonNode requestSchema = requestBody.path("content").path("application/json").path("schema");
        String schemaRef = requestSchema.path("$ref").asText();
        assertTrue(schemaRef.startsWith("#/components/schemas/"), "schema de entrada referenciado");
        JsonNode schema = document.path("components").path("schemas").path(refName(requestSchema.path("$ref")));
        assertRequired(schema, "bloodType", "rhFactor", "component", "quantity", "priority", "deliveryDeadline");
        assertEquals(Set.of("bloodType", "rhFactor", "component", "quantity", "priority", "deliveryDeadline"),
                objectFieldNames(schema.path("properties")));
        assertEquals("date-time", schema.path("properties").path("deliveryDeadline").path("format").asText());
        assertTrue(arrayValues(schema.path("properties").path("priority").path("enum"))
                .containsAll(Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL")));

        JsonNode scheme = document.path("components").path("securitySchemes").path("cookieAuth");
        assertEquals("apiKey", scheme.path("type").asText());
        assertEquals("cookie", scheme.path("in").asText());
        assertEquals("HEMOVIA_ACCESS", scheme.path("name").asText());
        JsonNode security = operation.has("security") ? operation.path("security") : document.path("security");
        boolean cookieRequired = false;
        for (JsonNode requirement : security) {
            if (requirement.has("cookieAuth")) cookieRequired = true;
        }
        assertTrue(cookieRequired, "sessão por cookie deve proteger a operação");
        boolean csrfRequired = false;
        for (JsonNode parameter : operation.path("parameters")) {
            if ("X-XSRF-TOKEN".equalsIgnoreCase(parameter.path("name").asText())
                    && "header".equals(parameter.path("in").asText())
                    && parameter.path("required").asBoolean()) csrfRequired = true;
        }
        assertTrue(csrfRequired, "cabeçalho CSRF obrigatório deve estar documentado");
        for (String code : Set.of("201", "400", "401", "403")) {
            assertTrue(operation.path("responses").has(code), "resposta " + code + " documentada");
        }
    }

    @Test
    @DisplayName("@spec:AC-032 OpenAPI documenta alocação, sessão, CSRF e respostas")
    void ac032DocumentsAllocationContract() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode operation = document.path("paths").path("/requests/{id}/allocate").path("post");
        assertFalse(operation.isMissingNode());
        assertTrue(hasPathIdParameter(operation));
        assertFalse(operation.has("requestBody"));
        assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
        JsonNode scheme = document.path("components").path("securitySchemes").path("cookieAuth");
        assertEquals("cookie", scheme.path("in").asText());
        assertEquals("HEMOVIA_ACCESS", scheme.path("name").asText());
        boolean csrfRequired = false;
        for (JsonNode parameter : operation.path("parameters")) {
            if ("X-XSRF-TOKEN".equalsIgnoreCase(parameter.path("name").asText())
                    && "header".equals(parameter.path("in").asText())
                    && parameter.path("required").asBoolean()) csrfRequired = true;
        }
        assertTrue(csrfRequired);
        for (String code : Set.of("201", "400", "401", "403", "404", "409", "422")) {
            JsonNode schema = operation.path("responses").path(code).path("content")
                    .path("application/json").path("schema");
            assertEquals(code.equals("201") ? "AllocationResponse" : "ApiError", refName(schema.path("$ref")),
                    "schema da resposta " + code);
        }
        JsonNode properties = document.path("components").path("schemas").path("AllocationResponse").path("properties");
        assertEquals(Set.of("id", "requestId", "status", "bloodBagIds", "route"), objectFieldNames(properties));
    }

    @Test
    @DisplayName("@spec:AC-047 OpenAPI documenta cancelamento de requisição")
    void ac047DocumentsCancellationContract() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode paths = document.path("paths");
        JsonNode operation = paths.path("/requests/{id}/cancel").path("post");
        assertFalse(operation.isMissingNode());
        assertFalse(paths.path("/requests").path("post").isMissingNode());
        assertFalse(paths.path("/requests/{id}/allocate").path("post").isMissingNode());
        assertFalse(operation.has("requestBody"));
        assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
        JsonNode scheme = document.path("components").path("securitySchemes").path("cookieAuth");
        assertEquals("cookie", scheme.path("in").asText());
        assertEquals("HEMOVIA_ACCESS", scheme.path("name").asText());
        boolean uuidPath = false;
        boolean csrfHeader = false;
        for (JsonNode parameter : operation.path("parameters")) {
            if ("id".equals(parameter.path("name").asText()) && "path".equals(parameter.path("in").asText())
                    && parameter.path("required").asBoolean()
                    && "uuid".equals(parameter.path("schema").path("format").asText())) uuidPath = true;
            if ("X-XSRF-TOKEN".equalsIgnoreCase(parameter.path("name").asText())
                    && "header".equals(parameter.path("in").asText()) && parameter.path("required").asBoolean()
                    && "string".equals(parameter.path("schema").path("type").asText())) csrfHeader = true;
        }
        assertTrue(uuidPath, "identificador UUID obrigatório");
        assertTrue(csrfHeader, "cabeçalho CSRF obrigatório");
        for (String code : Set.of("200", "400", "401", "403", "404", "422")) {
            JsonNode schema = operation.path("responses").path(code).path("content")
                    .path("application/json").path("schema");
            assertEquals(code.equals("200") ? "BloodRequestResponse" : "ApiError", refName(schema.path("$ref")),
                    "schema da resposta " + code);
        }
        JsonNode properties = document.path("components").path("schemas").path("BloodRequestResponse").path("properties");
        assertEquals(Set.of("id", "status"), objectFieldNames(properties));
    }

    @Test
    @DisplayName("@spec:AC-040 OpenAPI documenta o cadastro de hemocentros")
    void ac040DocumentsBloodCenterContract() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode collection = document.path("paths").path("/blood-centers");
        JsonNode member = document.path("paths").path("/blood-centers/{id}");
        assertBloodCenterOperation(collection.path("get"), false, Set.of("200", "401", "403"));
        assertBloodCenterOperation(collection.path("post"), true, Set.of("201", "400", "401", "403"));
        assertBloodCenterOperation(member.path("get"), false, Set.of("200", "400", "401", "403", "404"));
        assertBloodCenterOperation(member.path("put"), true, Set.of("200", "400", "401", "403", "404"));
        assertBloodCenterOperation(member.path("delete"), true, Set.of("204", "400", "401", "403", "404"));
        for (String method : Set.of("get", "put", "delete")) {
            assertTrue(hasPathIdParameter(member.path(method)));
        }
        JsonNode schemas = document.path("components").path("schemas");
        assertRequired(schemas.path("BloodCenterRequest"), "name", "city", "latitude", "longitude");
        assertEquals("uuid", schemas.path("BloodCenterResponse").path("properties").path("id").path("format").asText());
    }

    @Test
    @DisplayName("@spec:AC-054 OpenAPI documenta rede e prévia em JSON")
    void ac054DocumentsRouteOperations() throws Exception {
        JsonNode paths = getOpenApiDocument().path("paths");
        for (String path : Set.of("/route-graph/nodes", "/route-graph/edges")) {
            for (String method : Set.of("get", "post")) {
                JsonNode operation = paths.path(path).path(method);
                assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
                assertTrue(operation.path("responses").path(method.equals("get") ? "200" : "201")
                        .path("content").has("application/json"));
            }
        }
        JsonNode preview = paths.path("/routes/calculate").path("post");
        assertTrue(hasSecurityRequirement(preview, "cookieAuth"));
        assertTrue(preview.path("responses").path("200").path("content").has("application/json"));
        assertEquals("RoutePreviewResponse", refName(preview.path("responses").path("200")
                .path("content").path("application/json").path("schema").path("$ref")));
    }

    @Test
    @DisplayName("@spec:AC-066 OpenAPI descreve veículos e ciclo de transporte")
    void ac066DocumentsTransportAndVehicleContracts() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode schemas = document.path("components").path("schemas");
        assertRequired(schemas.path("StartTransportRequest"), "allocationId", "vehicleId");
        assertRequired(schemas.path("VehicleRequest"), "code", "capacity");
        for (String field : Set.of("allocationId", "vehicleId")) {
            assertEquals("uuid", schemas.path("StartTransportRequest").path("properties").path(field).path("format").asText());
        }
        JsonNode transport = schemas.path("TransportResponse").path("properties");
        for (String field : Set.of("id", "allocationId", "requestId", "routeId", "vehicleId")) {
            assertEquals("uuid", transport.path(field).path("format").asText());
        }
        for (String field : Set.of("startedAt", "completedAt")) {
            assertEquals("date-time", transport.path(field).path("format").asText());
        }
        for (String path : Set.of("/transports/start", "/transports/{id}/complete")) {
            JsonNode operation = document.path("paths").path(path).path("post");
            assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
            assertTrue(containsParameterNamed(operation, "X-XSRF-TOKEN"));
            for (String code : Set.of("400", "401", "403", "404", "422")) {
                assertEquals("ApiError", refName(operation.path("responses").path(code).path("content")
                        .path("application/json").path("schema").path("$ref")));
            }
            String success = path.endsWith("start") ? "201" : "200";
            assertEquals("TransportResponse", refName(operation.path("responses").path(success).path("content")
                    .path("application/json").path("schema").path("$ref")));
        }
        assertTrue(hasPathIdParameter(document.path("paths").path("/transports/{id}/complete").path("post")));
        assertTrue(document.path("paths").path("/transports/start").path("post").path("responses").has("409"));
        for (String method : Set.of("get", "post")) {
            JsonNode operation = document.path("paths").path("/vehicles").path(method);
            assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
            assertTrue(operation.path("responses").has(method.equals("get") ? "200" : "201"));
            assertEquals(method.equals("post"), containsParameterNamed(operation, "X-XSRF-TOKEN"));
        }
        assertFalse(schemas.has("Vehicle"));
        assertFalse(schemas.has("Transport"));
    }

    @Test
    @DisplayName("@spec:AC-074 OpenAPI documenta estatísticas, filtros e segurança")
    void ac074DocumentsStatisticsContract() throws Exception {
        JsonNode document = getOpenApiDocument();
        JsonNode schemas = document.path("components").path("schemas");
        JsonNode scheme = document.path("components").path("securitySchemes").path("cookieAuth");
        assertEquals("cookie", scheme.path("in").asText());
        assertEquals("HEMOVIA_ACCESS", scheme.path("name").asText());
        for (String name : Set.of("inventory", "demand")) {
            JsonNode operation = document.path("paths").path("/statistics/" + name).path("get");
            assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
            assertFalse(containsParameterNamed(operation, "X-XSRF-TOKEN"));
            for (String code : name.equals("demand") ? Set.of("200", "400", "401", "403") : Set.of("200", "401", "403")) {
                JsonNode schema = operation.path("responses").path(code).path("content").path("application/json").path("schema");
                assertEquals(code.equals("200") ? (name.equals("inventory") ? "InventorySummaryResponse" : "DemandSummaryResponse") : "ApiError", refName(schema.path("$ref")));
            }
        }
        JsonNode demand = document.path("paths").path("/statistics/demand").path("get");
        for (String name : Set.of("from", "to")) {
            boolean found = false;
            for (JsonNode parameter : demand.path("parameters")) {
                if (name.equals(parameter.path("name").asText())) {
                    found = true;
                    assertEquals("query", parameter.path("in").asText());
                    assertFalse(parameter.path("required").asBoolean());
                    assertEquals("date-time", parameter.path("schema").path("format").asText());
                }
            }
            assertTrue(found, name);
        }
        assertEquals(Set.of("generatedAt", "referenceDate", "totalBags", "byBloodType", "byComponent", "byBloodTypeAndComponent"), objectFieldNames(schemas.path("InventorySummaryResponse").path("properties")));
        assertEquals(Set.of("from", "to", "overall", "byHospital"), objectFieldNames(schemas.path("DemandSummaryResponse").path("properties")));
        JsonNode metrics = schemas.path("DemandMetricsResponse");
        assertEquals(Set.of("requestCount", "totalBags", "mean", "median", "populationVariance", "populationStandardDeviation", "sampleVariance", "sampleStandardDeviation", "insufficientSample", "warning"), objectFieldNames(metrics.path("properties")));
        assertEquals("int64", metrics.path("properties").path("requestCount").path("format").asText());
        assertEquals("int64", metrics.path("properties").path("totalBags").path("format").asText());
        assertTrue(metrics.path("description").asText().contains("bolsas"));
        assertEquals("int64", schemas.path("InventorySummaryResponse").path("properties").path("totalBags").path("format").asText());
        for (String name : Set.of("BloodTypeInventoryResponse", "ComponentInventoryResponse", "InventoryCombinationResponse")) {
            JsonNode properties = schemas.path(name).path("properties");
            assertEquals("int64", properties.path("count").path("format").asText());
            assertEquals("number", properties.path("percentage").path("type").asText());
            assertEquals(0, properties.path("percentage").path("minimum").asInt());
            assertEquals(100, properties.path("percentage").path("maximum").asInt());
            assertTrue(properties.path("percentage").path("description").asText().contains("duas casas"));
        }
        assertEquals(Set.of("hospitalId", "hospitalName", "metrics"), objectFieldNames(schemas.path("HospitalDemandResponse").path("properties")));
    }

    private static void assertBloodCenterOperation(JsonNode operation, boolean csrf, Set<String> responses) {
        assertTrue(hasSecurityRequirement(operation, "cookieAuth"));
        assertEquals(responses, objectFieldNames(operation.path("responses")));
        boolean csrfRequired = false;
        for (JsonNode parameter : operation.path("parameters")) {
            if ("X-XSRF-TOKEN".equalsIgnoreCase(parameter.path("name").asText())
                    && "header".equals(parameter.path("in").asText()) && parameter.path("required").asBoolean()) {
                csrfRequired = true;
            }
        }
        assertEquals(csrf, csrfRequired);
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
