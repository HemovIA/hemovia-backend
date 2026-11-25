package com.hemovia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.exception.RegistrationUnavailableException;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.repository.port.UserRepository;
import com.hemovia.api.service.port.output.PasswordHasher;
import com.hemovia.api.service.port.output.RegistrationEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(properties = {"spring.datasource.hikari.maximum-pool-size=2",
        "spring.datasource.hikari.minimum-idle=0"})
class DonorRegistrationIntegrationTest {
    private static final String AUTH = "/api/v1/auth";
    private static final String PASSWORD = "SenhaDeTeste!2026";
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired private WebApplicationContext context;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private PasswordHasher hasher;
    @MockitoBean private RegistrationEmailSender emailSender;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("@spec:AC-124 pedido público uniforme e sem conta antes da confirmação")
    void startIsPublicWithCsrfAndDoesNotCreateAccount() throws Exception {
        String newEmail = email();
        String takenEmail = email();
        users.save(User.create("Operador", new Email(takenEmail), new Cpf(cpf()), null,
                hasher.hash(PASSWORD), Role.ADMIN, null));
        mvc.perform(post(AUTH + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(Map.of("email", newEmail)))).andExpect(status().isForbidden());
        startAndGetToken(newEmail);
        start(newEmail).andExpect(status().isAccepted());
        start(takenEmail).andExpect(status().isAccepted()).andExpect(content().string(""))
                .andExpect(cookie().doesNotExist(SessionCookies.ACCESS));
        verify(emailSender, never()).send(eq(new Email(takenEmail)), anyString());
        assertThat(users.findByEmail(new Email(newEmail))).isEmpty();
        login(newEmail).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("@spec:AC-124 falha no envio permite solicitar outro link")
    void canRetryAfterEmailDeliveryFailure() throws Exception {
        String email = email();
        doThrow(new RegistrationUnavailableException()).doNothing()
                .when(emailSender).send(eq(new Email(email)), anyString());
        start(email).andExpect(status().isServiceUnavailable());
        assertThat(jdbc.queryForObject("select count(*) from donor_registration_tokens where email = ?", Integer.class, email))
                .isZero();
        start(email).andExpect(status().isAccepted());
        assertThat(jdbc.queryForObject("select count(*) from donor_registration_tokens where email = ?", Integer.class, email))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("@spec:AC-125 link válido registra hash e só pode ser usado uma vez")
    void validLinkCreatesLoginAndRejectsInvalidOrReusedLink() throws Exception {
        String email = email();
        String token = startAndGetToken(email);
        String stored = jdbc.queryForObject("select token_hash from donor_registration_tokens where email = ?", String.class, email);
        assertThat(stored).hasSize(64).isNotEqualTo(token);
        String cpf = cpf();
        String altered = (token.startsWith("a") ? "b" : "a") + token.substring(1);
        confirm(altered, cpf).andExpect(status().isBadRequest());
        confirm(token, "11111111111").andExpect(status().isBadRequest());
        confirm(token, cpf).andExpect(status().isCreated())
                .andExpect(cookie().doesNotExist(SessionCookies.ACCESS));
        User saved = users.findByEmail(new Email(email)).orElseThrow();
        assertThat(saved.passwordHash()).isNotEqualTo(PASSWORD);
        assertThat(hasher.matches(PASSWORD, saved.passwordHash())).isTrue();
        login(email).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("DOADOR"));
        login(masked(cpf)).andExpect(status().isOk());
        confirm(token, cpf()).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-125 link vencido não registra conta")
    void expiredLinkCannotCreateAccount() throws Exception {
        String email = email();
        String token = startAndGetToken(email);
        jdbc.update("""
                update donor_registration_tokens
                set issued_at = now() - interval '31 minutes', expires_at = now() - interval '1 minute'
                where email = ?
                """, email);
        confirm(token, cpf()).andExpect(status().isBadRequest());
        assertThat(users.findByEmail(new Email(email))).isEmpty();
    }

    @Test
    @DisplayName("@spec:AC-126 cadastro ignora papéis forçados e doador não acessa a operação")
    void publicAccountHasNoOperationalPermissions() throws Exception {
        String email = email();
        String token = startAndGetToken(email);
        mvc.perform(post(AUTH + "/register/confirm").with(csrf().asHeader()).contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(Map.of("token", token, "name", "Doadora", "cpf", cpf(),
                        "password", PASSWORD, "role", "ADMIN", "email", email(), "hospitalId", UUID.randomUUID()))))
                .andExpect(status().isCreated());
        var session = login(email).andExpect(status().isOk()).andReturn().getResponse();
        assertThat(session.getHeaders(HttpHeaders.SET_COOKIE)).isNotEmpty();
        var access = session.getCookie(SessionCookies.ACCESS);
        assertThat(access).isNotNull();
        mvc.perform(get(AUTH + "/me").cookie(access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("DOADOR"));
        for (String path : new String[]{"users", "donors", "hospitals", "requests", "transports", "donations", "blood-bags"}) {
            mvc.perform(get("/api/v1/" + path).cookie(access)).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("@spec:AC-127 CPF normalizado e e-mail confirmado vinculam a pessoa existente")
    void matchesTheExistingPersonWithoutChangingIt() throws Exception {
        String email = email();
        String cpf = cpf();
        UUID original = donor(cpf, email.toUpperCase(Locale.ROOT));
        UUID donation = UUID.randomUUID();
        jdbc.update("""
                insert into donations(id, collection_date, collection_center, donor_id)
                values (?, current_date, 'Hemocentro teste', ?)
                """, donation, original);
        confirm(startAndGetToken(email), masked(cpf)).andExpect(status().isCreated());
        var account = users.findByEmail(new Email(email)).orElseThrow();
        assertThat(account.donorId()).isEqualTo(original);
        assertThat(jdbc.queryForObject("select name from person where id = ?", String.class, original))
                .isEqualTo("Doação anterior");
        assertThat(jdbc.queryForObject("select donor_id from donations where id = ?", UUID.class, donation))
                .isEqualTo(original);
    }

    @Test
    @DisplayName("@spec:AC-128 CPF sem cadastro cria conta sem vínculo")
    void createsAnUnlinkedAccountWithoutCreatingAPerson() throws Exception {
        String email = email();
        String cpf = cpf();
        confirm(startAndGetToken(email), masked(cpf)).andExpect(status().isCreated());
        assertThat(users.findByEmail(new Email(email)).orElseThrow().donorId()).isNull();
        assertThat(jdbc.queryForObject("select count(*) from person where cpf = ?", Integer.class, cpf)).isZero();
    }

    @Test
    @DisplayName("@spec:AC-129 divergência ou ausência de e-mail preserva os cadastros de doador")
    void mismatchedOrMissingEmailRequiresUpdatingDonorRecords() throws Exception {
        String email = email();
        String anotherEmailCpf = cpf();
        String noEmailCpf = cpf();
        donor(anotherEmailCpf, email());
        donor(noEmailCpf, null);
        for (String cpf : new String[]{anotherEmailCpf, noEmailCpf}) {
            String attemptEmail = email();
            confirm(startAndGetToken(attemptEmail), cpf).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("hemocentro")));
            assertThat(users.findByEmail(new Email(attemptEmail))).isEmpty();
        }
        assertThat(users.findByEmail(new Email(email))).isEmpty();
    }

    @Test
    @DisplayName("@spec:AC-129 tentativas concorrentes com o mesmo CPF criam no máximo uma conta")
    void concurrentRequestsCannotClaimTheSameCpf() throws Exception {
        String token = startAndGetToken(email());
        String otherToken = startAndGetToken(email());
        String cpf = cpf();
        var gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                gate.await();
                return confirm(token, cpf).andReturn().getResponse().getStatus();
            });
            var other = executor.submit(() -> {
                gate.await();
                return confirm(otherToken, cpf).andReturn().getResponse().getStatus();
            });
            gate.countDown();
            assertThat(new int[]{first.get(15, TimeUnit.SECONDS), other.get(15, TimeUnit.SECONDS)})
                    .containsExactlyInAnyOrder(201, 409);
        }
        assertThat(jdbc.queryForObject("select count(*) from users where cpf = ?", Integer.class, cpf)).isEqualTo(1);
    }

    private ResultActions start(String email) throws Exception {
        return mvc.perform(post(AUTH + "/register").with(csrf().asHeader()).contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(Map.of("email", email))));
    }

    private String startAndGetToken(String email) throws Exception {
        start(email).andExpect(status().isAccepted()).andExpect(content().string(""))
                .andExpect(cookie().doesNotExist(SessionCookies.ACCESS));
        var token = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq(new Email(email)), token.capture());
        return token.getValue();
    }

    private ResultActions confirm(String token, String cpf) throws Exception {
        return mvc.perform(post(AUTH + "/register/confirm").with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(Map.of(
                        "token", token, "name", "Doadora", "cpf", cpf, "password", PASSWORD))));
    }

    private ResultActions login(String identifier) throws Exception {
        return mvc.perform(post(AUTH + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(Map.of("identifier", identifier, "password", PASSWORD))));
    }

    private UUID donor(String cpf, String email) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into person(id, name, cpf, email) values (?, 'Doação anterior', ?, ?)", id, cpf, email);
        return id;
    }

    private String cpf() {
        StringBuilder digits = new StringBuilder("73");
        for (int i = 2; i < 9; i++) {
            digits.append(ThreadLocalRandom.current().nextInt(10));
        }
        for (int n = 9; n <= 10; n++) {
            int total = 0;
            for (int i = 0; i < n; i++) {
                total += (digits.charAt(i) - '0') * (n + 1 - i);
            }
            int remainder = total % 11;
            digits.append(remainder < 2 ? 0 : 11 - remainder);
        }
        return digits.toString();
    }

    private String masked(String cpf) {
        return "%s.%s.%s-%s".formatted(cpf.substring(0, 3), cpf.substring(3, 6),
                cpf.substring(6, 9), cpf.substring(9));
    }

    private String email() {
        return "cadastro-" + UUID.randomUUID() + "@example.com";
    }
}
