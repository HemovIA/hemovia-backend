package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
class VehicleAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccessTokenIssuer issuer;
    MockMvc mvc;
    String code;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        code = "TEST-" + UUID.randomUUID();
    }
    @AfterEach void cleanup() { jdbc.update("delete from vehicles where code=?", code); }

    @Test @DisplayName("@spec:AC-065 administrador cadastra veículo e todos os papéis consultam")
    void ac065CreatesAndListsVehicles() throws Exception {
        create("{\"code\":\"  " + code + "  \",\"capacity\":3}", access(Role.ADMIN), true)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.code").value(code)).andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.hibernateLazyInitializer").doesNotExist());
        assertThat(jdbc.queryForObject("select count(*) from vehicles where code=? and capacity=3", Long.class, code)).isEqualTo(1);
        for (Role role : List.of(Role.ADMIN, Role.HEMOCENTRO, Role.HOSPITAL, Role.TRANSPORTADOR)) {
            mvc.perform(get("/api/v1/vehicles").cookie(access(role))).andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.code == '" + code + "')].capacity").value(hasItem(3)));
        }
        create("{\"code\":\"" + code + "\",\"capacity\":3}", access(Role.ADMIN), true).andExpect(status().isConflict());
        for (String invalid : List.of("{}", "{\"code\":\" \",\"capacity\":1}", "{\"code\":\"X\",\"capacity\":0}")) {
            create(invalid, access(Role.ADMIN), true).andExpect(status().isBadRequest());
        }
    }
    @Test @DisplayName("@spec:AC-065 cadastro exige sessão, papel e CSRF")
    void ac065ProtectsVehicleCreation() throws Exception {
        String body = "{\"code\":\"" + code + "\",\"capacity\":1}";
        create(body, null, true).andExpect(status().isUnauthorized());
        for (Role role : List.of(Role.HEMOCENTRO, Role.HOSPITAL, Role.TRANSPORTADOR))
            create(body, access(role), true).andExpect(status().isForbidden());
        create(body, access(Role.ADMIN), false).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/vehicles")).andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("select count(*) from vehicles where code=?", Long.class, code)).isZero();
    }
    private Cookie access(Role role) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, null, null, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private ResultActions create(String body, Cookie access, boolean csrf) throws Exception {
        var request = post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(body);
        if (access != null) request.cookie(access);
        if (csrf) { Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN"); request.cookie(token).header("X-XSRF-TOKEN", token.getValue()); }
        return mvc.perform(request);
    }
}
