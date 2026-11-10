package com.hemovia.api.presentation.controller;

import com.hemovia.api.application.port.output.AccessTokenIssuer;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import com.hemovia.api.infrastructure.security.SessionCookies;
import com.hemovia.api.model.Hospital;
import com.hemovia.api.repository.HospitalRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
class BloodRequestAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired HospitalRepository hospitals;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    MockMvc mvc;
    Hospital hospital;
    Instant deadline;
    final List<UUID> hospitalIds = new ArrayList<>();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        hospital = hospital();
        deadline = clock.now().plusSeconds(21600).truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    }

    @AfterEach
    void cleanup() {
        for (UUID id : hospitalIds) {
            jdbc.update("delete from blood_requests where hospital_id = ?", id);
            hospitals.deleteById(id);
        }
    }

    @Test @DisplayName("@spec:AC-022 criação persiste dados e estado pendente")
    void criaRequisicaoPendenteComDadosPersistidos() throws Exception {
        MvcResult response = send(body("4", ""), access(Role.HOSPITAL, hospital.getId()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").exists()).andReturn();
        UUID id = UUID.fromString(json.readTree(response.getResponse().getContentAsString()).get("id").asText());
        var row = jdbc.queryForMap("select hospital_id, blood_type, rh_factor, component, quantity, priority, delivery_deadline, status from blood_requests where id = ?", id);
        assertThat(row.get("hospital_id")).isEqualTo(hospital.getId());
        assertThat(row.get("blood_type")).isEqualTo("A");
        assertThat(row.get("rh_factor")).isEqualTo("POSITIVE");
        assertThat(row.get("component")).isEqualTo("PLATELETS");
        assertThat(row.get("quantity")).isEqualTo(4);
        assertThat(row.get("priority")).isEqualTo("HIGH");
        assertThat(((java.sql.Timestamp) row.get("delivery_deadline")).toInstant()).isEqualTo(deadline);
        assertThat(row.get("status")).isEqualTo("PENDING");
    }

    @Test @DisplayName("@spec:AC-023 quantidades ausentes e não positivas são rejeitadas")
    void rejeitaQuantidadesInvalidasSemPersistir() throws Exception {
        long before = count();
        Cookie access = access(Role.HOSPITAL, hospital.getId());
        for (String quantity : new String[]{null, "null", "0", "-1"}) {
            send(body(quantity, ""), access).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.quantity").exists());
            assertThat(count()).isEqualTo(before);
        }
        send(body("4", "").replace(deadline.toString(), clock.now().minusSeconds(1).toString()), access)
                .andExpect(status().isBadRequest());
        assertThat(count()).isEqualTo(before);
    }

    @Test @DisplayName("@spec:AC-024 sessão, papel e vínculo protegem a emissão")
    void exigeSessaoPapelHospitalEVinculo() throws Exception {
        long before = count();
        send(body("4", ""), null).andExpect(status().isUnauthorized());
        send(body("4", ""), access(Role.ADMIN, null)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/requests").cookie(access(Role.HOSPITAL, hospital.getId()))
                .contentType(MediaType.APPLICATION_JSON).content(body("4", ""))).andExpect(status().isForbidden());
        assertThat(count()).isEqualTo(before);
        Hospital other = hospital();
        MvcResult response = send(body("4", ",\"hospitalId\":\"" + other.getId() + "\",\"status\":\"APPROVED\""),
                access(Role.HOSPITAL, hospital.getId())).andExpect(status().isCreated()).andReturn();
        UUID id = UUID.fromString(json.readTree(response.getResponse().getContentAsString()).get("id").asText());
        var row = jdbc.queryForMap("select hospital_id, status from blood_requests where id = ?", id);
        assertThat(row.get("hospital_id")).isEqualTo(hospital.getId());
        assertThat(row.get("status")).isEqualTo("PENDING");
    }

    private Hospital hospital() {
        Hospital h = new Hospital("Hospital", "Recife");
        h.setLatitude(0.0); h.setLongitude(0.0); h.setPriority(1);
        Hospital saved = hospitals.saveAndFlush(h);
        hospitalIds.add(saved.getId());
        return saved;
    }

    private String body(String quantity, String extra) {
        String q = quantity == null ? "" : ",\"quantity\":" + quantity;
        return "{\"bloodType\":\"A\",\"rhFactor\":\"POSITIVE\",\"component\":\"PLATELETS\",\"priority\":\"HIGH\",\"deliveryDeadline\":\""
                + deadline + "\"" + q + extra + "}";
    }

    private Cookie access(Role role, UUID hospitalId) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"),
                null, "hash", role, true, hospitalId, null, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }

    private org.springframework.test.web.servlet.ResultActions send(String body, Cookie access) throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        Cookie token = csrf.getResponse().getCookie("XSRF-TOKEN");
        MockHttpServletRequestBuilder request = post("/api/v1/requests").cookie(token)
                .header("X-XSRF-TOKEN", token.getValue()).contentType(MediaType.APPLICATION_JSON).content(body);
        if (access != null) request.cookie(access);
        return mvc.perform(request);
    }

    private long count() { return jdbc.queryForObject("select count(*) from blood_requests", Long.class); }
}
