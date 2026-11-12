package com.hemovia.api.controller;

import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.service.port.output.ClockProvider;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
class BloodRequestAllocationAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    @Autowired ObjectMapper json;
    MockMvc mvc;
    UUID hospital, center, otherCenter;
    final List<UUID> requests = new ArrayList<>(), bags = new ArrayList<>();
    String trigger, function;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        hospital = UUID.randomUUID(); center = UUID.randomUUID(); otherCenter = UUID.randomUUID();
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?, 'H', 'C', 0, 0, 1)", hospital);
        for (UUID id : List.of(center, otherCenter)) jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?, 'C', 'C', 0, 0)", id);
    }
    @AfterEach void cleanup() {
        dropTrigger();
        for (UUID id : requests) {
            jdbc.update("delete from allocation_items where allocation_id in (select id from allocations where request_id = ?)", id);
            jdbc.update("delete from allocations where request_id = ?", id);
            jdbc.update("delete from blood_requests where id = ?", id);
        }
        for (UUID id : bags) jdbc.update("delete from blood_bags where id = ?", id);
        jdbc.update("delete from blood_centers where id in (?, ?)", center, otherCenter);
        jdbc.update("delete from hospital where id = ?", hospital);
    }
    @Test @DisplayName("@spec:AC-030 seleção compatível FEFO e reserva persistida")
    void ac030AllocatesCompatibleBagsInFefoOrder() throws Exception {
        UUID request = request("O", "POSITIVE", "RED_BLOOD_CELLS", 2, "PENDING");
        UUID first = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 2);
        UUID second = bag(center, "O", "NEGATIVE", "RED_BLOOD_CELLS", 5);
        UUID later = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 10);
        List<UUID> excluded = new ArrayList<>();
        excluded.add(bag(center, "A", "POSITIVE", "RED_BLOOD_CELLS", 1));
        excluded.add(bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", -1));
        excluded.add(bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 0));
        excluded.add(bag(center, "O", "POSITIVE", "PLATELETS", 1));
        excluded.add(bag(otherCenter, "O", "POSITIVE", "RED_BLOOD_CELLS", 1));
        excluded.add(bag(null, "O", "POSITIVE", "RED_BLOOD_CELLS", 1));
        UUID legacyBag = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 1); excluded.add(legacyBag);
        UUID legacyRequest = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "ALLOCATED");
        UUID legacyAllocation = UUID.randomUUID();
        jdbc.update("insert into allocations(id,request_id,blood_center_id,status,created_at,updated_at) values (?,?,?,'ALLOCATED',now(),now())", legacyAllocation, legacyRequest, center);
        jdbc.update("insert into allocation_items(id,allocation_id,blood_bag_id,status,created_at) values (?,?,?,'RESERVED',now())", UUID.randomUUID(), legacyAllocation, legacyBag);
        MvcResult result = send(request, access(Role.HEMOCENTRO), true).andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(request.toString())).andExpect(jsonPath("$.status").value("ALLOCATED"))
                .andExpect(jsonPath("$.bloodBagIds[0]").value(first.toString())).andExpect(jsonPath("$.bloodBagIds[1]").value(second.toString())).andReturn();
        UUID allocation = UUID.fromString(json.readTree(result.getResponse().getContentAsString()).get("id").asText());
        assertThat(jdbc.queryForMap("select request_id,blood_center_id,status from allocations where id=?", allocation))
                .containsEntry("request_id", request).containsEntry("blood_center_id", center).containsEntry("status", "ALLOCATED");
        assertThat(jdbc.queryForList("select blood_bag_id from allocation_items where allocation_id=? and status='RESERVED'", UUID.class, allocation)).containsExactlyInAnyOrder(first, second);
        assertThat(state(request)).isEqualTo("ALLOCATED");
        assertThat(bagState(first)).isEqualTo("RESERVED"); assertThat(bagState(second)).isEqualTo("RESERVED");
        excluded.add(later); for (UUID id : excluded) assertThat(bagState(id)).isEqualTo("AVAILABLE");
        assertThat(countItems(legacyRequest)).isEqualTo(1);
    }
    @Test @DisplayName("@spec:AC-031 estoque insuficiente não grava reserva parcial")
    void ac031RejectsInsufficientStock() throws Exception {
        UUID r = request("AB", "NEGATIVE", "RED_BLOOD_CELLS", 5, "PENDING");
        UUID a = bag(center, "O", "NEGATIVE", "RED_BLOOD_CELLS", 2), b = bag(center, "AB", "NEGATIVE", "RED_BLOOD_CELLS", 5);
        send(r, access(Role.HEMOCENTRO), true).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("INSUFFICIENT_INVENTORY")).andExpect(jsonPath("$.message").isNotEmpty());
        assertUnchanged(r, a, b);
    }
    @Test @DisplayName("@spec:AC-032 sessão, papel e CSRF protegem alocação")
    void ac032RequiresSessionRoleAndCsrf() throws Exception {
        UUID r = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "PENDING");
        UUID b = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 2);
        send(r, null, true).andExpect(status().isUnauthorized());
        for (Role role : List.of(Role.ADMIN, Role.HOSPITAL, Role.TRANSPORTADOR)) send(r, access(role), true).andExpect(status().isForbidden());
        send(r, access(Role.HEMOCENTRO), false).andExpect(status().isForbidden());
        assertUnchanged(r, b);
    }
    @Test @DisplayName("@spec:AC-033 requisição inexistente, processada ou componente inválido")
    void ac033RejectsInvalidRequests() throws Exception {
        UUID processed = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "ALLOCATED");
        UUID wrong = request("O", "POSITIVE", "PLATELETS", 1, "PENDING");
        UUID b = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 2);
        send(UUID.randomUUID(), access(Role.HEMOCENTRO), true).andExpect(status().isNotFound());
        send(processed, access(Role.HEMOCENTRO), true).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("REQUEST_ALREADY_PROCESSED"));
        send(wrong, access(Role.HEMOCENTRO), true).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").value("INVALID_STATE_TRANSITION"));
        assertThat(state(processed)).isEqualTo("ALLOCATED"); assertThat(countItems(processed)).isZero(); assertUnchanged(wrong, b);
    }
    @Test @DisplayName("@spec:AC-034 chamadas concorrentes disputam a última bolsa")
    void ac034OnlyOneConcurrentRequestReservesBag() throws Exception {
        UUID a = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "PENDING"), b = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "PENDING");
        UUID bag = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (Connection lock = dataSource.getConnection()) {
            lock.setAutoCommit(false);
            try (var statement = lock.prepareStatement("select id from blood_bags where id=? for update")) {
                statement.setObject(1, bag);
                statement.executeQuery().close();
            }
            CountDownLatch started = new CountDownLatch(2);
            Callable<MvcResult> callA = () -> { started.countDown(); return send(a, access(Role.HEMOCENTRO), true).andReturn(); };
            Callable<MvcResult> callB = () -> { started.countDown(); return send(b, access(Role.HEMOCENTRO), true).andReturn(); };
            Future<MvcResult> fa = pool.submit(callA), fb = pool.submit(callB);
            assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            int waiting = 0;
            while (System.nanoTime() < until) {
                waiting = jdbc.queryForObject("select count(*) from pg_stat_activity where datname=current_database() and wait_event_type='Lock' and lower(query) like '%blood_bags%'", Integer.class);
                if (waiting >= 2) break;
                Thread.yield();
            }
            assertThat(waiting).isGreaterThanOrEqualTo(2);
            lock.commit();
            MvcResult ra = fa.get(20, TimeUnit.SECONDS), rb = fb.get(20, TimeUnit.SECONDS);
            assertThat(List.of(ra.getResponse().getStatus(), rb.getResponse().getStatus())).containsExactlyInAnyOrder(201, 409);
            MvcResult loser = ra.getResponse().getStatus() == 409 ? ra : rb;
            assertThat(json.readTree(loser.getResponse().getContentAsString()).get("error").asText()).isEqualTo("INSUFFICIENT_INVENTORY");
            assertThat(List.of(state(a), state(b))).containsExactlyInAnyOrder("ALLOCATED", "PENDING");
            assertThat(countItems(a) + countItems(b)).isEqualTo(1);
            assertThat(bagState(bag)).isEqualTo("RESERVED");
        } finally {
            pool.shutdownNow();
            assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
        }
    }
    @Test @DisplayName("@spec:AC-035 falha no commit desfaz escritas e permite nova tentativa")
    void ac035RollsBackCommitFailureAndAllowsRetry() throws Exception {
        UUID r = request("O", "POSITIVE", "RED_BLOOD_CELLS", 1, "PENDING"), b = bag(center, "O", "POSITIVE", "RED_BLOOD_CELLS", 2);
        String suffix = UUID.randomUUID().toString().replace("-", ""); function = "fail_allocation_" + suffix; trigger = "fail_allocation_" + suffix;
        jdbc.execute("""
                create function %s() returns trigger language plpgsql as $$
                begin
                    if NEW.blood_bag_id = '%s'::uuid then
                        raise exception 'fixture failure';
                    end if;
                    return NEW;
                end;
                $$
                """.formatted(function, b));
        jdbc.execute("""
                create constraint trigger %s after insert on allocation_items
                deferrable initially deferred for each row execute function %s()
                """.formatted(trigger, function));
        send(r, access(Role.HEMOCENTRO), true).andExpect(status().isInternalServerError());
        assertUnchanged(r, b);
        dropTrigger();
        send(r, access(Role.HEMOCENTRO), true).andExpect(status().isCreated());
        assertThat(state(r)).isEqualTo("ALLOCATED"); assertThat(bagState(b)).isEqualTo("RESERVED"); assertThat(countItems(r)).isEqualTo(1);
    }
    private void dropTrigger() {
        if (trigger != null) { jdbc.execute("drop trigger if exists " + trigger + " on allocation_items"); trigger = null; }
        if (function != null) { jdbc.execute("drop function if exists " + function + "()"); function = null; }
    }
    private UUID request(String type, String rh, String component, int quantity, String status) {
        UUID id = UUID.randomUUID(); requests.add(id);
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority) values (?,?,?,?,?,?,?,now(),now(),'HIGH')", id,hospital,type,rh,component,quantity,status); return id;
    }
    private UUID bag(UUID centerId, String type, String rh, String component, int days) {
        UUID id = UUID.randomUUID(); bags.add(id); LocalDate today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,?,?,?,?,?,'AVAILABLE')", id,centerId,type,rh,component,today.minusDays(10),today.plusDays(days)); return id;
    }
    private Cookie access(Role role) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, null, center, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private ResultActions send(UUID id, Cookie access, boolean csrf) throws Exception {
        var request = post("/api/v1/requests/{id}/allocate", id);
        if (access != null) request.cookie(access);
        if (csrf) {
            Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
            request.cookie(token).header("X-XSRF-TOKEN", token.getValue());
        }
        return mvc.perform(request);
    }
    private String state(UUID id) { return jdbc.queryForObject("select status from blood_requests where id=?", String.class, id); }
    private String bagState(UUID id) { return jdbc.queryForObject("select status from blood_bags where id=?", String.class, id); }
    private long countItems(UUID id) { return jdbc.queryForObject("select count(*) from allocation_items where allocation_id in (select id from allocations where request_id=?)", Long.class, id); }
    private void assertUnchanged(UUID r, UUID... ids) {
        assertThat(state(r)).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("select count(*) from allocations where request_id=?", Long.class, r)).isZero();
        assertThat(countItems(r)).isZero(); for (UUID id : ids) assertThat(bagState(id)).isEqualTo("AVAILABLE");
    }
}
