package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.service.port.output.ClockProvider;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
class StatisticsAcceptanceTest {
    private static final String SCHEMA = "stats_" + UUID.randomUUID().toString().replace("-", "");
    private static final String INVENTORY = "/api/v1/statistics/inventory";
    private static final String DEMAND = "/api/v1/statistics/demand";
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    @Autowired ObjectMapper json;
    MockMvc mvc;
    UUID center;
    LocalDate today;

    @DynamicPropertySource
    static void schema(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.default-schema", () -> SCHEMA);
        registry.add("spring.flyway.schemas", () -> SCHEMA);
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
        registry.add("spring.datasource.hikari.schema", () -> SCHEMA);
    }

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        clean();
        center = UUID.randomUUID();
        today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?,'C','C',0,0)", center);
    }
    @AfterEach void cleanup() { clean(); }
    private void clean() {
        for (String table : List.of("allocation_items", "allocations", "blood_requests", "blood_bags", "hospital", "blood_centers"))
            jdbc.update("delete from " + table);
    }

    @Test @DisplayName("@spec:AC-067 distribuição de 200 bolsas reconcilia todas as categorias")
    void ac067ReconcilesInventoryDistribution() throws Exception {
        for (int i = 0; i < 80; i++) bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(5));
        for (int i = 0; i < 20; i++) bag(center, "AB", "NEGATIVE", "PLASMA", "AVAILABLE", today.plusDays(5));
        for (int i = 0; i < 100; i++) bag(center, "A", "POSITIVE", "PLATELETS", "AVAILABLE", today.plusDays(5));
        JsonNode body = read(INVENTORY, Role.ADMIN);
        assertThat(body.path("totalBags").asLong()).isEqualTo(200);
        assertGroups(body, 200, 100);
        assertCategory(body.path("byBloodType"), "O", "POSITIVE", 80, "40");
        assertCategory(body.path("byBloodType"), "AB", "NEGATIVE", 20, "10");
        assertCategory(body.path("byBloodType"), "A", "POSITIVE", 100, "50");
        assertComponent(body, "O", "POSITIVE", "RED_BLOOD_CELLS", 80);
        assertComponent(body, "AB", "NEGATIVE", "PLASMA", 20);
        assertComponent(body, "A", "POSITIVE", "PLATELETS", 100);
        assertThat(jdbc.queryForObject("select count(*) from blood_bags", Long.class)).isEqualTo(200);
    }

    @Test @DisplayName("@spec:AC-068 vazio e frações retornam percentuais consistentes")
    void ac068HandlesEmptyAndFractionalInventory() throws Exception {
        JsonNode empty = read(INVENTORY, Role.HEMOCENTRO);
        assertThat(empty.path("totalBags").asLong()).isZero();
        assertGroups(empty, 0, 0);
        bag(center, "A", "POSITIVE", "PLASMA", "AVAILABLE", today.plusDays(1));
        bag(center, "B", "POSITIVE", "PLATELETS", "AVAILABLE", today.plusDays(1));
        bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(1));
        assertGroups(read(INVENTORY, Role.ADMIN), 3, 100);
        assertThat(jdbc.queryForObject("select count(*) from blood_bags", Long.class)).isEqualTo(3);
    }

    @Test @DisplayName("@spec:AC-069 somente bolsas elegíveis sem alterações persistidas")
    void ac069ExcludesUnavailableBagsWithoutMutation() throws Exception {
        bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(2));
        bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today);
        bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.minusDays(1));
        bag(null, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(2));
        UUID deleted = UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude,deleted_at) values (?,'D','C',0,0,now())", deleted);
        bag(deleted, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(2));
        for (String state : List.of("RESERVED", "IN_TRANSIT", "DELIVERED", "DISCARDED"))
            bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", state, today.plusDays(2));
        UUID request = request(hospital("A"), 2, "ALLOCATED", clock.now());
        UUID allocation = UUID.randomUUID();
        jdbc.update("insert into allocations(id,request_id,blood_center_id,status,created_at,updated_at) values (?,?,?,'ALLOCATED',now(),now())", allocation, request, center);
        for (String state : List.of("RESERVED", "IN_TRANSIT")) {
            UUID id = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(2));
            jdbc.update("insert into allocation_items(id,allocation_id,blood_bag_id,status,created_at) values (?,?,?,?,now())", UUID.randomUUID(), allocation, id, state);
        }
        List<List<Map<String, Object>>> before = snapshot();
        assertThat(read(INVENTORY, Role.ADMIN).path("totalBags").asLong()).isEqualTo(1);
        assertThat(snapshot()).isEqualTo(before);
    }

    @Test @DisplayName("@spec:AC-070 métricas populacionais e amostrais por requisição e hospital")
    void ac070CalculatesDemandMetrics() throws Exception {
        request(hospital("A"), 10, "PENDING", clock.now());
        request(hospital("B"), 20, "ALLOCATED", clock.now());
        request(hospital("C"), 30, "DELIVERED", clock.now());
        JsonNode body = read(DEMAND, Role.ADMIN);
        metrics(body.path("overall"), 3, 60, "20", "20", "66.67", "8.16", "100", "10", false);
        assertThat(body.path("byHospital").size()).isEqualTo(3);
        for (JsonNode h : body.path("byHospital")) {
            assertThat(h.path("metrics").path("requestCount").asLong()).isEqualTo(1);
            assertThat(h.path("metrics").path("insufficientSample").asBoolean()).isTrue();
        }
        assertThat(jdbc.queryForObject("select count(*) from blood_requests", Long.class)).isEqualTo(3);
    }

    @Test @DisplayName("@spec:AC-071 vazio, unidade e frequências repetidas preservam observações")
    void ac071HandlesEmptySingleAndRepeatedDemand() throws Exception {
        JsonNode empty = read(DEMAND, Role.ADMIN);
        metrics(empty.path("overall"), 0, 0, "0", "0", "0", "0", "0", "0", true);
        assertThat(empty.path("byHospital").size()).isZero();
        UUID a = hospital("A"), b = hospital("B");
        request(a, 10, "PENDING", clock.now());
        metrics(read(DEMAND, Role.ADMIN).path("overall"), 1, 10, "10", "10", "0", "0", "0", "0", true);
        request(a, 10, "PENDING", clock.now());
        request(a, 20, "PENDING", clock.now());
        request(b, 30, "PENDING", clock.now());
        JsonNode body = read(DEMAND, Role.ADMIN);
        metrics(body.path("overall"), 4, 70, "17.5", "15", "68.75", "8.29", "91.67", "9.57", false);
        long count = 0;
        for (JsonNode h : body.path("byHospital")) {
            count += h.path("metrics").path("requestCount").asLong();
            if (h.path("hospitalId").asText().equals(a.toString())) assertThat(h.path("metrics").path("requestCount").asLong()).isEqualTo(3);
        }
        assertThat(count).isEqualTo(4);
        assertThat(jdbc.queryForObject("select count(*) from blood_requests", Long.class)).isEqualTo(4);
    }

    @Test @DisplayName("@spec:AC-072 limites temporais, estados e cancelamentos")
    void ac072FiltersByCreatedAtAndExcludesCancelled() throws Exception {
        UUID a = hospital("A");
        Instant from = Instant.parse("2024-01-01T00:00:00Z"), to = Instant.parse("2024-02-01T00:00:00Z");
        request(a, 1, "PENDING", from.minusSeconds(1));
        request(a, 2, "PENDING", from);
        request(a, 3, "ALLOCATED", from.plusSeconds(1));
        request(a, 4, "IN_TRANSIT", from.plusSeconds(2));
        request(a, 5, "DELIVERED", from.plusSeconds(3));
        request(a, 99, "CANCELLED", from.plusSeconds(4));
        request(a, 6, "PENDING", to);
        request(a, 7, "PENDING", to.plusSeconds(1));
        assertCount(DEMAND, 7, 28);
        assertCount(DEMAND + "?from=2024-01-01T00:00:00Z&to=2024-02-01T00:00:00Z", 4, 14);
        assertCount(DEMAND + "?from=2024-01-01T00:00:00Z", 6, 27);
        assertCount(DEMAND + "?to=2024-02-01T00:00:00Z", 5, 15);
        assertCount(DEMAND + "?from=2023-12-31T21:00:00-03:00&to=2024-01-31T21:00:00-03:00", 4, 14);
        for (String query : List.of("?from=invalid", "?from=2024-01-01T00:00:00Z&to=2024-01-01T00:00:00Z", "?from=2024-02-01T00:00:00Z&to=2024-01-01T00:00:00Z"))
            mvc.perform(get(DEMAND + query).cookie(access(Role.ADMIN))).andExpect(status().isBadRequest());
        assertThat(jdbc.queryForObject("select count(*) from blood_requests", Long.class)).isEqualTo(8);
    }

    @Test @DisplayName("@spec:AC-073 segurança, privacidade e consultas sem escrita")
    void ac073ProtectsReadsAndDoesNotMutate() throws Exception {
        bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", "AVAILABLE", today.plusDays(3));
        request(hospital("A"), 10, "PENDING", clock.now());
        List<List<Map<String, Object>>> before = snapshot();
        for (String path : List.of(INVENTORY, DEMAND)) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            for (Role role : List.of(Role.HOSPITAL, Role.TRANSPORTADOR)) mvc.perform(get(path).cookie(access(role))).andExpect(status().isForbidden());
            mvc.perform(get(path).cookie(access(null))).andExpect(status().isForbidden());
            for (Role role : List.of(Role.ADMIN, Role.HEMOCENTRO)) {
                String body = mvc.perform(get(path).cookie(access(role))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
                assertThat(body).doesNotContain("password", "donor", "cpf", "email", "hibernateLazyInitializer", "version");
            }
        }
        assertThat(snapshot()).isEqualTo(before);
    }

    private Cookie access(Role role) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, null, null, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private JsonNode read(String path, Role role) throws Exception {
        return json.readTree(mvc.perform(get(path).cookie(access(role))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }
    private UUID hospital(String name) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?,?,'C',0,0,1)", id, name);
        return id;
    }
    private UUID request(UUID hospital, int quantity, String state, Instant created) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,?,?,'HIGH')", id, hospital, quantity, state, Timestamp.from(created), Timestamp.from(created));
        return id;
    }
    private UUID bag(UUID centerId, String type, String rh, String component, String state, LocalDate expiry) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,?,?,?,?,?,?)", id, centerId, type, rh, component, today.minusDays(10), expiry, state);
        return id;
    }
    private List<List<Map<String, Object>>> snapshot() {
        return List.of(jdbc.queryForList("select * from blood_bags order by id"), jdbc.queryForList("select * from blood_requests order by id"), jdbc.queryForList("select * from allocations order by id"), jdbc.queryForList("select * from allocation_items order by id"), jdbc.queryForList("select * from blood_centers order by id"));
    }
    private void assertCount(String path, int count, int total) throws Exception {
        JsonNode overall = read(path, Role.ADMIN).path("overall");
        assertThat(overall.path("requestCount").asLong()).isEqualTo(count);
        assertThat(overall.path("totalBags").asLong()).isEqualTo(total);
    }
    private static void assertGroups(JsonNode body, int count, int percentage) {
        String[] groups = {"byBloodType", "byComponent", "byBloodTypeAndComponent"};
        int[] sizes = {8, 5, 40};
        for (int i = 0; i < groups.length; i++) {
            JsonNode group = body.path(groups[i]);
            assertThat(group.size()).isEqualTo(sizes[i]);
            long sum = 0;
            BigDecimal percent = BigDecimal.ZERO;
            for (JsonNode entry : group) { sum += entry.path("count").asLong(); percent = percent.add(entry.path("percentage").decimalValue()); }
            assertThat(sum).isEqualTo(count);
            assertThat(percent).isEqualByComparingTo(BigDecimal.valueOf(percentage));
        }
    }
    private static void assertCategory(JsonNode group, String type, String rh, int count, String percent) {
        for (JsonNode entry : group) if (entry.path("bloodType").asText().equals(type) && entry.path("rhFactor").asText().equals(rh)) {
            assertThat(entry.path("count").asLong()).isEqualTo(count);
            assertThat(entry.path("percentage").decimalValue()).isEqualByComparingTo(percent);
            return;
        }
        throw new AssertionError("Categoria ausente: " + type + " " + rh);
    }
    private static void assertComponent(JsonNode body, String type, String rh, String component, int count) {
        boolean componentFound = false, combinationFound = false;
        for (JsonNode entry : body.path("byComponent")) {
            if (entry.path("component").asText().equals(component)) {
                componentFound = true;
                assertThat(entry.path("count").asLong()).isEqualTo(count);
                assertThat(entry.path("percentage").decimalValue()).isEqualByComparingTo(BigDecimal.valueOf(count / 2));
            }
        }
        for (JsonNode entry : body.path("byBloodTypeAndComponent")) {
            if (entry.path("bloodType").asText().equals(type) && entry.path("rhFactor").asText().equals(rh)
                    && entry.path("component").asText().equals(component)) {
                combinationFound = true;
                assertThat(entry.path("count").asLong()).isEqualTo(count);
                assertThat(entry.path("percentage").decimalValue()).isEqualByComparingTo(BigDecimal.valueOf(count / 2));
            }
        }
        assertThat(componentFound).isTrue();
        assertThat(combinationFound).isTrue();
    }
    private static void metrics(JsonNode m, int count, int total, String mean, String median, String pv, String ps, String sv, String ss, boolean insufficient) {
        assertThat(m.path("requestCount").asLong()).isEqualTo(count);
        assertThat(m.path("totalBags").asLong()).isEqualTo(total);
        String[] fields = {"mean", "median", "populationVariance", "populationStandardDeviation", "sampleVariance", "sampleStandardDeviation"};
        String[] expected = {mean, median, pv, ps, sv, ss};
        for (int i = 0; i < fields.length; i++) assertThat(m.path(fields[i]).decimalValue()).isEqualByComparingTo(expected[i]);
        assertThat(m.path("insufficientSample").asBoolean()).isEqualTo(insufficient);
        if (insufficient) assertThat(m.path("warning").asText()).contains("n < 2");
        else assertThat(m.path("warning").isNull()).isTrue();
    }
}
