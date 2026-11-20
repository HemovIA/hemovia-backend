package com.hemovia.api.controller;

import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.User;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.controller.security.SessionCookies;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
class BloodCenterAcceptanceTest {
    private static final String PATH = "/api/v1/blood-centers";
    private static final String BODY = """
            {"name":"Hemocentro Central","city":"Recife","latitude":-8.05,"longitude":-34.9}
            """;

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ObjectMapper json;

    private final List<UUID> centers = new ArrayList<>();
    private MockMvc mvc;
    private Cookie admin;
    private UUID bag;
    private UUID hospital;
    private UUID request;
    private UUID operator;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = session(Role.ADMIN, null);
    }

    @AfterEach
    void cleanup() {
        if (request != null) {
            jdbc.update("delete from allocation_items where allocation_id in (select id from allocations where request_id = ?)", request);
            jdbc.update("delete from allocations where request_id = ?", request);
            jdbc.update("delete from blood_requests where id = ?", request);
        }
        if (hospital != null) jdbc.update("delete from hospital where id = ?", hospital);
        if (bag != null) jdbc.update("delete from blood_bags where id = ?", bag);
        if (operator != null) jdbc.update("delete from users where id = ?", operator);
        centers.forEach(id -> jdbc.update("delete from blood_centers where id = ?", id));
    }

    @Test
    @DisplayName("@spec:AC-036 administrador cadastra, consulta e atualiza o hemocentro")
    void ac036ManagesBloodCenter() throws Exception {
        UUID id = createCenter();
        var reader = session(Role.HEMOCENTRO, id);
        mvc.perform(get(PATH).cookie(reader)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.toString())));
        mvc.perform(get(PATH + "/{id}", id).cookie(reader)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hemocentro Central"))
                .andExpect(jsonPath("$.city").value("Recife"));

        String updated = """
                {"name":"Hemocentro Regional","city":"Olinda","latitude":-8.01,"longitude":-34.85}
                """;
        send(put(PATH + "/{id}", id), admin, updated, true).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Hemocentro Regional"))
                .andExpect(jsonPath("$.city").value("Olinda"));
        mvc.perform(get(PATH + "/{id}", id).cookie(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(-8.01))
                .andExpect(jsonPath("$.longitude").value(-34.85));
    }

    @Test
    @DisplayName("@spec:AC-037 dados inválidos, falta de sessão, papel ou CSRF não alteram o cadastro")
    void ac037RejectsUnauthorizedAndInvalidWrites() throws Exception {
        UUID id = createCenter();
        var reader = session(Role.HEMOCENTRO, id);
        send(post(PATH), null, BODY, true).andExpect(status().isUnauthorized());
        mvc.perform(get(PATH)).andExpect(status().isUnauthorized());
        send(post(PATH), reader, BODY, true).andExpect(status().isForbidden());
        send(put(PATH + "/{id}", id), reader, BODY, true).andExpect(status().isForbidden());
        send(delete(PATH + "/{id}", id), reader, null, true).andExpect(status().isForbidden());
        send(post(PATH), admin, BODY, false).andExpect(status().isForbidden());
        send(put(PATH + "/{id}", id), admin, BODY, false).andExpect(status().isForbidden());
        send(delete(PATH + "/{id}", id), admin, null, false).andExpect(status().isForbidden());

        for (String invalid : List.of(
                BODY.replace("\"Recife\"", "\"  \""),
                BODY.replace("-8.05", "90.01"),
                BODY.replace("-34.9", "-180.01"),
                BODY.replace("-8.05", "null"),
                BODY.replace("Hemocentro Central", "X".repeat(256)))) {
            send(post(PATH), admin, invalid, true).andExpect(status().isBadRequest());
            send(put(PATH + "/{id}", id), admin, invalid, true).andExpect(status().isBadRequest());
        }
        assertThat(jdbc.queryForMap("select name, city, latitude, longitude, deleted_at from blood_centers where id = ?", id))
                .containsEntry("name", "Hemocentro Central").containsEntry("city", "Recife")
                .containsEntry("latitude", -8.05).containsEntry("longitude", -34.9).containsEntry("deleted_at", null);
    }

    @Test
    @DisplayName("@spec:AC-038 excluir oculta o cadastro e preserva a linha e a bolsa histórica")
    void ac038SoftDeletesPreservingHistory() throws Exception {
        UUID id = createCenter();
        UUID other = createCenter();
        insertBag(id);
        send(delete(PATH + "/{id}", id), admin, null, true).andExpect(status().isNoContent());

        assertThat(jdbc.queryForMap("select name, deleted_at from blood_centers where id = ?", id))
                .containsEntry("name", "Hemocentro Central").doesNotContainEntry("deleted_at", null);
        mvc.perform(get("/api/v1/blood-bags/{id}", bag).cookie(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.bloodCenterId").value(id.toString()));
        send(put("/api/v1/blood-bags/{id}", bag), admin, bagBody(id), true).andExpect(status().isOk())
                .andExpect(jsonPath("$.bloodCenterId").value(id.toString()));
        mvc.perform(get(PATH).cookie(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(id.toString()))))
                .andExpect(jsonPath("$[*].id", hasItem(other.toString())));
        mvc.perform(get(PATH + "/{id}", id).cookie(admin)).andExpect(status().isNotFound());
        send(put(PATH + "/{id}", id), admin, BODY, true).andExpect(status().isNotFound());
        send(delete(PATH + "/{id}", id), admin, null, true).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-039 centro excluído não recebe novos vínculos nem aloca bolsas")
    void ac039RejectsNewOperationsAtDeletedCenter() throws Exception {
        UUID id = createCenter();
        UUID other = createCenter();
        insertBag(id);
        insertPendingRequest();
        operator = UUID.randomUUID();
        jdbc.update("""
                insert into users(id, name, email, cpf, password, role, blood_center_id)
                values (?, 'Operador', ?, '52998224725', 'fixture', 'HEMOCENTRO', ?)
                """, operator, operator + "@example.com", other);
        var oldSession = session(Role.HEMOCENTRO, id);
        send(delete(PATH + "/{id}", id), admin, null, true).andExpect(status().isNoContent());

        send(post("/api/v1/users"), admin, """
                {"name":"Novo operador","email":"%s@example.com","cpf":"11144477735",
                 "password":"teste-ficticio","role":"HEMOCENTRO","bloodCenterId":"%s"}
                """.formatted(UUID.randomUUID(), id), true).andExpect(status().isNotFound());
        send(patch("/api/v1/users/{id}/role", operator), admin, """
                {"role":"HEMOCENTRO","bloodCenterId":"%s"}
                """.formatted(id), true).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("select blood_center_id from users where id = ?", UUID.class, operator))
                .isEqualTo(other);

        var today = LocalDate.now(ZoneOffset.UTC);
        send(post("/api/v1/blood-bags"), admin, bagBody(id), true).andExpect(status().isNotFound());
        send(post("/api/v1/donations"), admin, """
                {"collectionCenter":"Centro","collectionDate":"%s","expirationDate":"%s",
                 "bloodType":"O","rhFactor":"POSITIVE","component":"RED_BLOOD_CELLS","bloodCenterId":"%s"}
                """.formatted(today, today.plusDays(10), id), true).andExpect(status().isNotFound());
        send(post("/api/v1/requests/{id}/allocate", request), oldSession, null, true)
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("NOT_FOUND"));

        assertThat(jdbc.queryForObject("select status from blood_requests where id = ?", String.class, request))
                .isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("select status from blood_bags where id = ?", String.class, bag))
                .isEqualTo("AVAILABLE");
        assertThat(jdbc.queryForObject("select count(*) from blood_bags where blood_center_id = ?", Long.class, id))
                .isEqualTo(1L);
        for (String table : List.of("users", "donations", "allocations")) {
            assertThat(jdbc.queryForObject("select count(*) from " + table + " where blood_center_id = ?", Long.class, id))
                    .isZero();
        }
    }

    private UUID createCenter() throws Exception {
        var result = send(post(PATH), admin, BODY, true).andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith(PATH + "/")))
                .andExpect(jsonPath("$.latitude").value(-8.05)).andReturn();
        UUID id = UUID.fromString(json.readTree(result.getResponse().getContentAsString()).path("id").asText());
        centers.add(id);
        return id;
    }

    private ResultActions send(MockHttpServletRequestBuilder call, Cookie access, String body, boolean csrf)
            throws Exception {
        if (access != null) call.cookie(access);
        if (body != null) call.contentType(MediaType.APPLICATION_JSON).content(body);
        if (csrf) {
            Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
            call.cookie(token).header("X-XSRF-TOKEN", token.getValue());
        }
        return mvc.perform(call);
    }

    private Cookie session(Role role, UUID centerId) {
        var user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"),
                new Cpf("52998224725"), null, "fixture", role, true, null, centerId, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }

    private static String bagBody(UUID centerId) {
        var today = LocalDate.now(ZoneOffset.UTC);
        return """
                {"rhFactor":"POSITIVE","component":"RED_BLOOD_CELLS","collectionDate":"%sT00:00:00Z",
                 "expirationDate":"%sT00:00:00Z","status":"AVAILABLE","bloodTypeEnum":"O","bloodCenterId":"%s"}
                """.formatted(today, today.plusDays(10), centerId);
    }

    private void insertBag(UUID centerId) {
        bag = UUID.randomUUID();
        jdbc.update("""
                insert into blood_bags(id, blood_center_id, blood_type_enum, rh_factor, component,
                                       collection_date, expiration_date, status)
                values (?, ?, 'O', 'POSITIVE', 'RED_BLOOD_CELLS', current_date, current_date + 10, 'AVAILABLE')
                """, bag, centerId);
    }

    private void insertPendingRequest() {
        hospital = UUID.randomUUID();
        request = UUID.randomUUID();
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?, 'H', 'C', 0, 0, 1)", hospital);
        jdbc.update("""
                insert into blood_requests(id, hospital_id, blood_type, rh_factor, component, quantity,
                                           status, created_at, updated_at, priority)
                values (?, ?, 'O', 'POSITIVE', 'RED_BLOOD_CELLS', 1, 'PENDING', now(), now(), 'HIGH')
                """, request, hospital);
    }
}
