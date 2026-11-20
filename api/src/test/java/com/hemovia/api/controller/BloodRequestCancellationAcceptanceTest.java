package com.hemovia.api.controller;

import com.hemovia.api.support.RouteGraphFixtures;
import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.service.port.output.ClockProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
class BloodRequestCancellationAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    MockMvc mvc;
    UUID hospital, otherHospital, center;
    final List<UUID> requests = new ArrayList<>(), bags = new ArrayList<>(), routes = new ArrayList<>(), vehicles = new ArrayList<>();
    String trigger, function;
    UUID centerNode, hospitalNode;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        hospital = UUID.randomUUID(); otherHospital = UUID.randomUUID(); center = UUID.randomUUID();
        for (UUID id : List.of(hospital, otherHospital)) jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?, 'H', 'C', 0, 0, 1)", id);
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?, 'C', 'C', 0, 0)", center);
        centerNode = RouteGraphFixtures.center(jdbc, center);
        hospitalNode = RouteGraphFixtures.hospital(jdbc, hospital);
        RouteGraphFixtures.edge(jdbc, centerNode, hospitalNode, 60);
    }
    @AfterEach void cleanup() {
        dropTrigger();
        for (UUID id : requests) {
            jdbc.update("delete from transports where allocation_id in (select id from allocations where request_id=?)", id);
            jdbc.update("delete from allocation_items where allocation_id in (select id from allocations where request_id=?)", id);
            jdbc.update("delete from route_waypoints where route_id in (select route_id from allocations where request_id=?)", id);
            jdbc.update("delete from delivery_route_nodes where route_id in (select route_id from allocations where request_id=?)", id);
            List<UUID> allocatedRoutes = jdbc.queryForList("select route_id from allocations where request_id=? and route_id is not null", UUID.class, id);
            jdbc.update("delete from allocations where request_id=?", id);
            for (UUID route : allocatedRoutes) jdbc.update("delete from delivery_routes where id=?", route);
            jdbc.update("delete from blood_requests where id=?", id);
        }
        for (UUID id : routes) jdbc.update("delete from delivery_routes where id=?", id);
        for (UUID id : vehicles) jdbc.update("delete from vehicles where id=?", id);
        for (UUID id : bags) jdbc.update("delete from blood_bags where id=?", id);
        RouteGraphFixtures.cleanup(jdbc, centerNode, hospitalNode);
        jdbc.update("delete from blood_centers where id=?", center);
        jdbc.update("delete from hospital where id in (?,?)", hospital, otherHospital);
    }
    @Test @DisplayName("@spec:AC-041 @spec:AC-056 cancelamento libera três bolsas e preserva o histórico da rota")
    void ac041ReleasesAllBagsAndAllowsReallocation() throws Exception {
        UUID r = request(hospital, 3, "PENDING");
        UUID a = bag(), b = bag(), c = bag();
        allocate(r).andExpect(status().isCreated());
        UUID allocation = allocation(r);
        UUID route = jdbc.queryForObject("select route_id from allocations where id=?", UUID.class, allocation);
        List<UUID> itemIds = itemIds(allocation);
        assertThat(itemIds).hasSize(3);
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(r.toString())).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertReleased(r, allocation, itemIds, a, b, c);
        assertThat(jdbc.queryForObject("select route_id from allocations where id=?", UUID.class, allocation)).isEqualTo(route);
        assertThat(count("select count(*) from delivery_routes where id=?", route)).isEqualTo(1);
        UUID next = request(hospital, 3, "PENDING");
        allocate(next).andExpect(status().isCreated());
        assertThat(jdbc.queryForList("select blood_bag_id from allocation_items where allocation_id=? and status='RESERVED'", UUID.class, allocation(next))).containsExactlyInAnyOrder(a,b,c);
        assertThat(itemIds(allocation)).containsExactlyInAnyOrderElementsOf(itemIds);
        assertThat(count("select count(*) from allocation_items where allocation_id=? and status='RELEASED' and released_at is not null", allocation)).isEqualTo(3);
    }
    @Test @DisplayName("@spec:AC-042 pedido pendente não cria alocação nem altera outra reserva")
    void ac042CancelsPendingWithoutTouchingOtherReservation() throws Exception {
        UUID other = request(hospital, 1, "PENDING"), b = bag(); allocate(other).andExpect(status().isCreated());
        UUID r = request(hospital, 1, "PENDING");
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(r.toString())).andExpect(jsonPath("$.status").value("CANCELLED"));
        assertThat(count("select count(*) from allocations where request_id=?", r)).isZero();
        assertReserved(other, allocation(other), b);
    }
    @Test @DisplayName("@spec:AC-043 estados finais e transporte em trânsito impedem cancelamento")
    void ac043RejectsInvalidStatesAndInTransitTransport() throws Exception {
        for (String state : List.of("IN_TRANSIT", "DELIVERED", "CANCELLED")) {
            UUID r = request(hospital, 1, state);
            cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").value("INVALID_STATE_TRANSITION"));
            assertThat(state(r)).isEqualTo(state); assertThat(count("select count(*) from allocations where request_id=?", r)).isZero();
        }
        UUID r = request(hospital, 1, "PENDING"), b = bag(); allocate(r).andExpect(status().isCreated());
        UUID allocation = allocation(r), route = UUID.randomUUID(), vehicle = UUID.randomUUID(), transport = UUID.randomUUID();
        routes.add(route); vehicles.add(vehicle);
        jdbc.update("insert into delivery_routes(id,origin_id,destination_id,distance_km,estimated_duration_seconds) values (?,?,?,1,1)", route, center, hospital);
        jdbc.update("insert into vehicles(id,code,capacity) values (?,?,1)", vehicle, vehicle.toString());
        jdbc.update("insert into transports(id,allocation_id,route_id,vehicle_id,status,started_at) values (?,?,?,?,'IN_TRANSIT',now())", transport, allocation, route, vehicle);
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").value("INVALID_STATE_TRANSITION"));
        assertReserved(r, allocation, b);
        assertThat(jdbc.queryForObject("select status from transports where id=?", String.class, transport)).isEqualTo("IN_TRANSIT");
        assertThat(count("select count(*) from transports where id=? and allocation_id=?", transport, allocation)).isEqualTo(1);
    }
    @Test @DisplayName("@spec:AC-044 sessão papel CSRF propriedade e UUID protegem cancelamento")
    void ac044EnforcesSecurityAndOwnership() throws Exception {
        UUID r = request(hospital, 1, "PENDING"), b = bag(); allocate(r).andExpect(status().isCreated()); UUID allocation = allocation(r);
        cancel(r.toString(), null, true).andExpect(status().isUnauthorized());
        for (Role role : List.of(Role.ADMIN, Role.HEMOCENTRO, Role.TRANSPORTADOR)) cancel(r.toString(), access(role, hospital), true).andExpect(status().isForbidden());
        cancel(r.toString(), access(Role.HOSPITAL, hospital), false).andExpect(status().isForbidden());
        cancel(r.toString(), access(Role.HOSPITAL, otherHospital), true).andExpect(status().isNotFound());
        cancel(UUID.randomUUID().toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isNotFound());
        cancel("invalid-uuid", access(Role.HOSPITAL, hospital), true).andExpect(status().isBadRequest());
        assertReserved(r, allocation, b);
    }
    @Test @DisplayName("@spec:AC-045 falha no commit desfaz o cancelamento e permite repetição")
    void ac045RollsBackCommitFailureAndAllowsRetry() throws Exception {
        UUID r = request(hospital, 1, "PENDING"), b = bag(); allocate(r).andExpect(status().isCreated()); UUID allocation = allocation(r);
        String suffix = UUID.randomUUID().toString().replace("-", ""); function = "fail_cancel_" + suffix; trigger = function;
        jdbc.execute("create function " + function + "() returns trigger language plpgsql as $$ begin if NEW.blood_bag_id = '" + b + "'::uuid then raise exception 'fixture failure'; end if; return NEW; end; $$");
        jdbc.execute("create constraint trigger " + trigger + " after update on allocation_items deferrable initially deferred for each row execute function " + function + "()");
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isInternalServerError());
        assertReserved(r, allocation, b);
        List<UUID> ids = itemIds(allocation); dropTrigger();
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isOk());
        assertReleased(r, allocation, ids, b);
    }
    @Test @DisplayName("@spec:AC-046 cancelamento e alocação concorrentes coordenam a requisição")
    void ac046CoordinatesCancellationAndAllocation() throws Exception {
        UUID r = request(hospital, 1, "PENDING"), b = bag();
        List<Integer> statuses = race(r, () -> cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andReturn(), () -> allocate(r).andReturn());
        assertThat(statuses.get(0)).isEqualTo(200);
        assertThat(statuses.get(1)).isIn(201, 409);
        assertThat(state(r)).isEqualTo("CANCELLED");
        assertThat(bagState(b)).isEqualTo("AVAILABLE");
        assertThat(count("select count(*) from allocation_items where blood_bag_id=? and status in ('RESERVED','IN_TRANSIT')", b)).isZero();
        assertThat(count("select count(*) from allocations where request_id=? and status <> 'CANCELLED'", r)).isZero();
        assertThat(count("select count(*) from allocation_items i join allocations a on a.id=i.allocation_id where a.request_id=? and (i.status <> 'RELEASED' or i.released_at is null)", r)).isZero();
    }
    @Test @DisplayName("@spec:AC-046 cancelamentos concorrentes preservam outras reservas")
    void ac046CoordinatesTwoCancellations() throws Exception {
        UUID r = request(hospital, 1, "PENDING"), b = bag(); allocate(r).andExpect(status().isCreated()); UUID allocation = allocation(r); List<UUID> ids = itemIds(allocation);
        UUID other = request(hospital, 1, "PENDING"), otherBag = bag(); allocate(other).andExpect(status().isCreated()); UUID otherAllocation = allocation(other);
        List<Integer> statuses = race(r, () -> cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andReturn(), () -> cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andReturn());
        assertThat(statuses).containsExactlyInAnyOrder(200, 422);
        assertReleased(r, allocation, ids, b); assertReserved(other, otherAllocation, otherBag);
        cancel(r.toString(), access(Role.HOSPITAL, hospital), true).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.error").value("INVALID_STATE_TRANSITION"));
    }
    private List<Integer> race(UUID id, Callable<MvcResult> first, Callable<MvcResult> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (Connection lock = dataSource.getConnection()) {
            lock.setAutoCommit(false);
            try (var s = lock.prepareStatement("select id from blood_requests where id=? for update")) { s.setObject(1, id); s.executeQuery().close(); }
            CountDownLatch started = new CountDownLatch(2);
            Future<MvcResult> a = pool.submit(() -> { started.countDown(); return first.call(); });
            Future<MvcResult> b = pool.submit(() -> { started.countDown(); return second.call(); });
            assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); int waiting = 0;
            while (System.nanoTime() < until) {
                waiting = jdbc.queryForObject("select count(*) from pg_stat_activity where datname=current_database() and wait_event_type='Lock' and lower(query) like '%blood_requests%'", Integer.class);
                if (waiting >= 2) break; Thread.yield();
            }
            assertThat(waiting).isGreaterThanOrEqualTo(2); lock.commit();
            return List.of(a.get(20, TimeUnit.SECONDS).getResponse().getStatus(), b.get(20, TimeUnit.SECONDS).getResponse().getStatus());
        } finally { pool.shutdownNow(); assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue(); }
    }
    private UUID request(UUID owner, int quantity, String status) {
        UUID id = UUID.randomUUID(); requests.add(id);
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,now(),now(),'HIGH')", id, owner, quantity, status); return id;
    }
    private UUID bag() {
        UUID id = UUID.randomUUID(); bags.add(id); var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'AVAILABLE')", id, center, today.minusDays(10), today.plusDays(10)); return id;
    }
    private Cookie access(Role role, UUID owner) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, owner, center, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private ResultActions allocate(UUID id) throws Exception { return send(id.toString(), "allocate", access(Role.HEMOCENTRO, null), true); }
    private ResultActions cancel(String id, Cookie access, boolean csrf) throws Exception { return send(id, "cancel", access, csrf); }
    private ResultActions send(String id, String action, Cookie access, boolean csrf) throws Exception {
        var request = post("/api/v1/requests/{id}/" + action, id);
        if (access != null) request.cookie(access);
        if (csrf) { Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN"); request.cookie(token).header("X-XSRF-TOKEN", token.getValue()); }
        return mvc.perform(request);
    }
    private UUID allocation(UUID r) { return jdbc.queryForObject("select id from allocations where request_id=?", UUID.class, r); }
    private List<UUID> itemIds(UUID a) { return jdbc.queryForList("select id from allocation_items where allocation_id=?", UUID.class, a); }
    private String state(UUID r) { return jdbc.queryForObject("select status from blood_requests where id=?", String.class, r); }
    private String bagState(UUID b) { return jdbc.queryForObject("select status from blood_bags where id=?", String.class, b); }
    private long count(String sql, Object... args) { return jdbc.queryForObject(sql, Long.class, args); }
    private void assertReserved(UUID r, UUID a, UUID b) {
        assertThat(state(r)).isEqualTo("ALLOCATED"); assertThat(bagState(b)).isEqualTo("RESERVED");
        assertThat(count("select count(*) from allocations where id=? and request_id=? and status='ALLOCATED'", a, r)).isEqualTo(1);
        assertThat(count("select count(*) from allocation_items where allocation_id=? and blood_bag_id=? and status='RESERVED' and released_at is null", a, b)).isEqualTo(1);
    }
    private void assertReleased(UUID r, UUID a, List<UUID> ids, UUID... releasedBags) {
        assertThat(state(r)).isEqualTo("CANCELLED");
        assertThat(count("select count(*) from allocations where id=? and request_id=? and status='CANCELLED'", a, r)).isEqualTo(1);
        assertThat(itemIds(a)).containsExactlyInAnyOrderElementsOf(ids);
        assertThat(count("select count(*) from allocation_items where allocation_id=? and status='RELEASED' and released_at is not null", a)).isEqualTo(ids.size());
        for (UUID b : releasedBags) assertThat(bagState(b)).isEqualTo("AVAILABLE");
    }
    private void dropTrigger() {
        if (trigger != null) { jdbc.execute("drop trigger if exists " + trigger + " on allocation_items"); trigger = null; }
        if (function != null) { jdbc.execute("drop function if exists " + function + "()"); function = null; }
    }
}
