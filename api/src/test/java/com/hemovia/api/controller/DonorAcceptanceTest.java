package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration") @SpringBootTest
class DonorAcceptanceTest {
    static final String PATH = "/api/v1/donors";
    static final String BODY = "{\"name\":\"Doador sintético\",\"cpf\":\"529.982.247-25\"}";
    @Autowired WebApplicationContext context;
    @Autowired AccessTokenIssuer issuer;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }
    @AfterEach void cleanup() { jdbc.update("delete from person where cpf = ?", "52998224725"); }
    Cookie session(Role role) {
        var user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "fixture", role, true, null, null, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    int create(Cookie access) throws Exception {
        Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        return mvc.perform(post(PATH).cookie(access, token).header("X-XSRF-TOKEN", token.getValue()).contentType(MediaType.APPLICATION_JSON).content(BODY)).andReturn().getResponse().getStatus();
    }
    @Test @DisplayName("@spec:AC-095 CPF exato, validação, permissão e concorrência no banco")
    void exactSearchSecurityAndConcurrentUniqueness() throws Exception {
        var admin = session(Role.ADMIN);
        mvc.perform(post(PATH).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        for (Role role : new Role[]{Role.HOSPITAL, Role.TRANSPORTADOR}) {
            assertEquals(403, create(session(role)));
        }
        mvc.perform(get(PATH).param("cpf", "529").cookie(admin)).andExpect(status().isBadRequest());
        mvc.perform(get(PATH).param("cpf", "52998224725").cookie(admin)).andExpect(status().isNotFound());
        mvc.perform(get(PATH).param("cpf", "52998224725")).andExpect(status().isUnauthorized());
        for (Role role : new Role[]{Role.HOSPITAL, Role.TRANSPORTADOR})
            mvc.perform(get(PATH).param("cpf", "52998224725").cookie(session(role))).andExpect(status().isForbidden());
        var executor = Executors.newFixedThreadPool(2);
        try {
            var gate = new CountDownLatch(1);
            var a = executor.submit(() -> { gate.await(); return create(admin); });
            var b = executor.submit(() -> { gate.await(); return create(session(Role.HEMOCENTRO)); });
            gate.countDown();
            assertEquals(java.util.Set.of(201, 409), java.util.Set.of(a.get(), b.get()));
        } finally { executor.shutdownNow(); }
        mvc.perform(get(PATH).param("cpf", "529.982.247-25").cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Doador sintético")).andExpect(jsonPath("$.cpf").value("52998224725"));
        assertEquals(1, jdbc.queryForObject("select count(*) from person where cpf = ?", Integer.class, "52998224725"));
    }
}

