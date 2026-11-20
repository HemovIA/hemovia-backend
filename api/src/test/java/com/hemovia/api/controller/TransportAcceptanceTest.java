package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.support.RouteGraphFixtures;
import jakarta.servlet.http.Cookie;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.*;
import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.test.web.servlet.MvcResult;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
class TransportAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    MockMvc mvc;
    UUID center, hospital, origin, destination, request, bag, vehicle, allocation, route;
    String trigger;

    @BeforeEach void setup() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        center = UUID.randomUUID(); hospital = UUID.randomUUID(); request = UUID.randomUUID(); bag = UUID.randomUUID(); vehicle = UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?,'C','C',0,0)", center);
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?,'H','C',0,1,1)", hospital);
        origin = RouteGraphFixtures.center(jdbc, center); destination = RouteGraphFixtures.hospital(jdbc, hospital);
        RouteGraphFixtures.edge(jdbc, origin, destination, 60);
        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'AVAILABLE')", bag, center, today.minusDays(1), today.plusDays(5));
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority,delivery_deadline) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',1,'PENDING',now(),now(),'HIGH',?)", request, hospital, java.sql.Timestamp.from(clock.now().plusSeconds(86400)));
        jdbc.update("insert into vehicles(id,code,capacity) values (?,?,1)", vehicle, vehicle.toString());
        send("/api/v1/requests/" + request + "/allocate", null, access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isCreated());
        allocation = jdbc.queryForObject("select id from allocations where request_id=?", UUID.class, request);
        route = jdbc.queryForObject("select route_id from allocations where id=?", UUID.class, allocation);
    }
    @AfterEach void cleanup() {
        dropTrigger();
        jdbc.update("delete from transports where allocation_id=?", allocation);
        jdbc.update("delete from allocation_items where allocation_id=?", allocation);
        jdbc.update("delete from route_waypoints where route_id=?", route);
        jdbc.update("delete from delivery_route_nodes where route_id=?", route);
        jdbc.update("delete from allocations where id=?", allocation);
        jdbc.update("delete from delivery_routes where id=?", route);
        jdbc.update("delete from blood_requests where id=?", request);
        jdbc.update("delete from blood_bags where id=?", bag);
        jdbc.update("delete from vehicles where id=?", vehicle);
        RouteGraphFixtures.cleanup(jdbc, origin, destination);
        jdbc.update("delete from blood_centers where id=?", center);
        jdbc.update("delete from hospital where id=?", hospital);
    }
    @Test @DisplayName("@spec:AC-057 @spec:AC-061 início e entrega atualizam todo o lote e preservam a rota")
    void ac057And061CompleteLifecycle() throws Exception {
        List<UUID> extraBags = new java.util.ArrayList<>();
        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        try {
            for (int n = 0; n < 3; n++) {
                UUID extra = UUID.randomUUID(); extraBags.add(extra);
                jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'RESERVED')", extra, center, today.minusDays(1), today.plusDays(5));
                jdbc.update("insert into allocation_items(id,allocation_id,blood_bag_id,status,created_at) values (?,?,?,'RESERVED',now())", UUID.randomUUID(), allocation, extra);
            }
            jdbc.update("update blood_requests set quantity=4 where id=?", request);
            jdbc.update("update vehicles set capacity=4 where id=?", vehicle);
            start().andExpect(status().isCreated()).andExpect(jsonPath("$.allocationId").value(allocation.toString()))
                    .andExpect(jsonPath("$.requestId").value(request.toString())).andExpect(jsonPath("$.routeId").value(route.toString()))
                    .andExpect(jsonPath("$.vehicleId").value(vehicle.toString())).andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                    .andExpect(jsonPath("$.startedAt").isNotEmpty());
            UUID id = transport();
            assertThat(jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id)).isNull();
            assertThat(jdbc.queryForObject("select status from transports where id=?", String.class, id)).isEqualTo("IN_TRANSIT");
            assertBatchStates("IN_TRANSIT");
            var started = jdbc.queryForObject("select started_at from transports where id=?", java.sql.Timestamp.class, id);
            var before = clock.now();
            complete(id).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DELIVERED"))
                    .andExpect(jsonPath("$.routeId").value(route.toString())).andExpect(jsonPath("$.completedAt").isNotEmpty());
            assertBatchStates("DELIVERED");
            assertThat(jdbc.queryForObject("select started_at from transports where id=?", java.sql.Timestamp.class, id)).isEqualTo(started);
            assertThat(jdbc.queryForObject("select count(*) from transports where vehicle_id=? and status='IN_TRANSIT'", Long.class, vehicle)).isZero();
            var completed = jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id);
            assertThat(completed.toInstant()).isBetween(before, clock.now());
            assertThat(jdbc.queryForObject("select status from transports where id=?", String.class, id)).isEqualTo("DELIVERED");
            assertThat(jdbc.queryForObject("select count(*) from allocation_items where allocation_id=? and released_at is null", Long.class, allocation)).isEqualTo(4);
            complete(id).andExpect(status().isUnprocessableEntity());
            assertThat(jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id)).isEqualTo(completed);
        } finally {
            for (UUID extra : extraBags) {
                jdbc.update("delete from allocation_items where blood_bag_id=?", extra);
                jdbc.update("delete from blood_bags where id=?", extra);
            }
        }
    }
    private void assertBatchStates(String state) {
        assertThat(jdbc.queryForObject("select status from blood_requests where id=?", String.class, request)).isEqualTo(state);
        assertThat(jdbc.queryForObject("select status from allocations where id=?", String.class, allocation)).isEqualTo(state);
        assertThat(jdbc.queryForObject("select count(*) from allocation_items where allocation_id=? and status=? and released_at is null", Long.class, allocation, state)).isEqualTo(4);
        assertThat(jdbc.queryForObject("select count(*) from blood_bags where id in (select blood_bag_id from allocation_items where allocation_id=?) and status=?", Long.class, allocation, state)).isEqualTo(4);
    }
    @Test @DisplayName("@spec:AC-061 entrega libera veículo para nova viagem sem reutilizar bolsa entregue")
    void ac061ReusesVehicleAfterDelivery() throws Exception {
        start().andExpect(status().isCreated()); complete(transport()).andExpect(status().isOk());
        UUID nextRequest = UUID.randomUUID(), nextBag = UUID.randomUUID();
        UUID nextAllocation = null, nextRoute = null;
        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'AVAILABLE')", nextBag, center, today.minusDays(1), today.plusDays(5));
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority,delivery_deadline) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',1,'PENDING',now(),now(),'HIGH',?)", nextRequest, hospital, java.sql.Timestamp.from(clock.now().plusSeconds(86400)));
        try {
            send("/api/v1/requests/" + nextRequest + "/allocate", null, access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isCreated());
            nextAllocation = jdbc.queryForObject("select id from allocations where request_id=?", UUID.class, nextRequest);
            nextRoute = jdbc.queryForObject("select route_id from allocations where id=?", UUID.class, nextAllocation);
            assertThat(jdbc.queryForObject("select blood_bag_id from allocation_items where allocation_id=?", UUID.class, nextAllocation)).isEqualTo(nextBag);
            String body = "{\"allocationId\":\"" + nextAllocation + "\",\"vehicleId\":\"" + vehicle + "\"}";
            send("/api/v1/transports/start", body, access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isCreated());
            assertThat(jdbc.queryForObject("select status from blood_bags where id=?", String.class, bag)).isEqualTo("DELIVERED");
            assertThat(jdbc.queryForObject("select status from blood_bags where id=?", String.class, nextBag)).isEqualTo("IN_TRANSIT");
            assertThat(jdbc.queryForObject("select count(*) from transports where vehicle_id=? and status='IN_TRANSIT'", Long.class, vehicle)).isEqualTo(1);
        } finally {
            jdbc.update("delete from transports where allocation_id=?", nextAllocation);
            jdbc.update("delete from allocation_items where allocation_id=?", nextAllocation);
            jdbc.update("delete from route_waypoints where route_id=?", nextRoute);
            jdbc.update("delete from delivery_route_nodes where route_id=?", nextRoute);
            jdbc.update("delete from allocations where id=?", nextAllocation);
            jdbc.update("delete from delivery_routes where id=?", nextRoute);
            jdbc.update("delete from blood_requests where id=?", nextRequest);
            jdbc.update("delete from blood_bags where id=?", nextBag);
        }
    }
    @Test @DisplayName("@spec:AC-058 estados e lote inválidos impedem início")
    void ac058RejectsInvalidStartStates() throws Exception {
        for (String state : new String[]{"PENDING", "CANCELLED", "IN_TRANSIT", "DELIVERED"}) {
            jdbc.update("update blood_requests set status=? where id=?", state, request);
            start().andExpect(status().isUnprocessableEntity());
            assertThat(jdbc.queryForObject("select status from blood_requests where id=?", String.class, request)).isEqualTo(state);
        }
        jdbc.update("update blood_requests set status='ALLOCATED', quantity=2 where id=?", request);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_requests set quantity=1 where id=?", request);
        jdbc.update("update blood_bags set status='AVAILABLE' where id=?", bag);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_bags set status='RESERVED' where id=?", bag);
        jdbc.update("update allocation_items set status='RELEASED', released_at=now() where allocation_id=?", allocation);
        start().andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForObject("select count(*) from transports where allocation_id=?", Long.class, allocation)).isZero();
    }
    @Test @DisplayName("@spec:AC-058 rota, validade, compatibilidade e prazo são verificados antes da partida")
    void ac059RejectsInvalidDepartureConditions() throws Exception {
        jdbc.update("update allocations set route_id=null where id=?", allocation);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update allocations set route_id=? where id=?", route, allocation);
        jdbc.update("update delivery_routes set estimated_duration_seconds=0 where id=?", route);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update delivery_routes set estimated_duration_seconds=60 where id=?", route);
        jdbc.update("update blood_requests set delivery_deadline=? where id=?", java.sql.Timestamp.from(clock.now().minusSeconds(1)), request);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_requests set delivery_deadline=? where id=?", java.sql.Timestamp.from(clock.now().plusSeconds(86400)), request);
        jdbc.update("update blood_bags set expiration_date=? where id=?", clock.now().atZone(ZoneOffset.UTC).toLocalDate(), bag);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_bags set expiration_date=?, blood_type_enum='A' where id=?", clock.now().atZone(ZoneOffset.UTC).toLocalDate().plusDays(5), bag);
        start().andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_bags set blood_type_enum='O' where id=?", bag);
        assertStates("ALLOCATED", "ALLOCATED", "RESERVED", "RESERVED");
        assertThat(jdbc.queryForObject("select count(*) from transports where allocation_id=?", Long.class, allocation)).isZero();
    }
    @Test @DisplayName("@spec:AC-059 capacidade insuficiente impede partida sem alterar reservas")
    void ac059RejectsInsufficientCapacity() throws Exception {
        UUID second = UUID.randomUUID(); var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'RESERVED')", second, center, today.minusDays(1), today.plusDays(5));
        try {
            jdbc.update("insert into allocation_items(id,allocation_id,blood_bag_id,status,created_at) values (?,?,?,'RESERVED',now())", UUID.randomUUID(), allocation, second);
            jdbc.update("update blood_requests set quantity=2 where id=?", request);
            start().andExpect(status().isUnprocessableEntity());
            assertThat(jdbc.queryForObject("select count(*) from allocation_items where allocation_id=? and status='RESERVED'", Long.class, allocation)).isEqualTo(2);
            assertThat(jdbc.queryForObject("select count(*) from blood_bags where id in (?,?) and status='RESERVED'", Long.class, bag, second)).isEqualTo(2);
            assertThat(jdbc.queryForObject("select count(*) from transports where allocation_id=?", Long.class, allocation)).isZero();
        } finally {
            jdbc.update("delete from allocation_items where blood_bag_id=?", second);
            jdbc.update("delete from blood_bags where id=?", second);
        }
    }
    @Test @DisplayName("@spec:AC-062 conclusão rejeita lote inconsistente e estados finais")
    void ac062RejectsInconsistentCompletion() throws Exception {
        start().andExpect(status().isCreated()); UUID id = transport();
        for (String state : new String[]{"PLANNED", "CANCELLED", "DELIVERED"}) {
            jdbc.update("update transports set status=? where id=?", state, id);
            complete(id).andExpect(status().isUnprocessableEntity());
        }
        jdbc.update("update transports set status='IN_TRANSIT' where id=?", id);
        jdbc.update("update blood_bags set status='RESERVED' where id=?", bag);
        complete(id).andExpect(status().isUnprocessableEntity());
        jdbc.update("update blood_bags set status='IN_TRANSIT' where id=?", bag);
        assertStates("IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT");
        assertThat(jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id)).isNull();
    }
    @Test @DisplayName("@spec:AC-063 falha tardia desfaz início e entrega e permite repetição")
    void ac063RollsBackBothCommitFailures() throws Exception {
        failOnItemUpdate();
        start().andExpect(status().isInternalServerError());
        assertStates("ALLOCATED", "ALLOCATED", "RESERVED", "RESERVED");
        assertThat(jdbc.queryForObject("select count(*) from transports where allocation_id=?", Long.class, allocation)).isZero();
        dropTrigger(); start().andExpect(status().isCreated()); UUID id = transport();
        failOnItemUpdate(); complete(id).andExpect(status().isInternalServerError());
        assertStates("IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT");
        assertThat(jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id)).isNull();
        dropTrigger(); complete(id).andExpect(status().isOk()); assertStates("DELIVERED", "DELIVERED", "DELIVERED", "DELIVERED");
    }
    @Test @DisplayName("@spec:AC-064 início e entrega exigem sessão, papel, propriedade e CSRF")
    void ac064ProtectsTransportEndpoints() throws Exception {
        String body = "{\"allocationId\":\"" + allocation + "\",\"vehicleId\":\"" + vehicle + "\"}";
        String path = "/api/v1/transports/start";
        send(path, body, null, true).andExpect(status().isUnauthorized());
        for (Role role : new Role[]{Role.ADMIN, Role.HOSPITAL, Role.TRANSPORTADOR})
            send(path, body, access(role, hospital, center), true).andExpect(status().isForbidden());
        send(path, body, access(Role.HEMOCENTRO, hospital, center), false).andExpect(status().isForbidden());
        send(path, body, access(Role.HEMOCENTRO, hospital, UUID.randomUUID()), true).andExpect(status().isNotFound());
        send(path, "{}", access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isBadRequest());
        send(path, "{\"allocationId\":\"invalid\",\"vehicleId\":\"" + vehicle + "\"}", access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isBadRequest());
        send(path, "{\"allocationId\":\"" + UUID.randomUUID() + "\",\"vehicleId\":\"" + vehicle + "\"}", access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isNotFound());
        send(path, "{\"allocationId\":\"" + allocation + "\",\"vehicleId\":\"" + UUID.randomUUID() + "\"}", access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isNotFound());
        send("/api/v1/transports/" + UUID.randomUUID() + "/complete", null, access(Role.HOSPITAL, hospital, center), true).andExpect(status().isNotFound());
        start().andExpect(status().isCreated()); UUID id = transport();
        String complete = "/api/v1/transports/" + id + "/complete";
        send(complete, null, null, true).andExpect(status().isUnauthorized());
        for (Role role : new Role[]{Role.ADMIN, Role.HEMOCENTRO, Role.TRANSPORTADOR})
            send(complete, null, access(role, hospital, center), true).andExpect(status().isForbidden());
        send(complete, null, access(Role.HOSPITAL, hospital, center), false).andExpect(status().isForbidden());
        send(complete, null, access(Role.HOSPITAL, UUID.randomUUID(), center), true).andExpect(status().isNotFound());
        send("/api/v1/transports/invalid/complete", null, access(Role.HOSPITAL, hospital, center), true).andExpect(status().isBadRequest());
        assertStates("IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT");
    }
    @Test @DisplayName("@spec:AC-060 inícios concorrentes da mesma alocação produzem uma viagem")
    void ac060CoordinatesConcurrentStarts() throws Exception {
        assertThat(race(() -> start().andReturn(), () -> start().andReturn())).containsExactlyInAnyOrder(201, 422);
        assertStates("IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT");
        assertThat(jdbc.queryForObject("select count(*) from transports where allocation_id=?", Long.class, allocation)).isEqualTo(1);
    }
    @Test @DisplayName("@spec:AC-059 @spec:AC-060 alocações concorrentes disputam o mesmo veículo")
    void ac060CoordinatesVehicleCompetition() throws Exception {
        UUID otherRequest = UUID.randomUUID(), otherBag = UUID.randomUUID();
        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'AVAILABLE')", otherBag, center, today.minusDays(1), today.plusDays(5));
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority,delivery_deadline) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',1,'PENDING',now(),now(),'HIGH',?)", otherRequest, hospital, java.sql.Timestamp.from(clock.now().plusSeconds(86400)));
        UUID otherAllocation = null, otherRoute = null;
        try {
            send("/api/v1/requests/" + otherRequest + "/allocate", null, access(Role.HEMOCENTRO, hospital, center), true).andExpect(status().isCreated());
            otherAllocation = jdbc.queryForObject("select id from allocations where request_id=?", UUID.class, otherRequest);
            otherRoute = jdbc.queryForObject("select route_id from allocations where id=?", UUID.class, otherAllocation);
            UUID target = otherAllocation;
            String body = "{\"allocationId\":\"" + target + "\",\"vehicleId\":\"" + vehicle + "\"}";
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try (Connection lock = dataSource.getConnection()) {
                lock.setAutoCommit(false);
                try (var statement = lock.prepareStatement("select id from vehicles where id=? for update")) { statement.setObject(1, vehicle); statement.executeQuery().close(); }
                Future<MvcResult> a = pool.submit(() -> start().andReturn());
                Future<MvcResult> b = pool.submit(() -> send("/api/v1/transports/start", body, access(Role.HEMOCENTRO, hospital, center), true).andReturn());
                long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); int waiting = 0;
                while (System.nanoTime() < until) {
                    waiting = jdbc.queryForObject("select count(*) from pg_stat_activity where datname=current_database() and wait_event_type='Lock' and lower(query) like '%vehicles%'", Integer.class);
                    if (waiting >= 2) break;
                    Thread.yield();
                }
                assertThat(waiting).isGreaterThanOrEqualTo(2); lock.commit();
                MvcResult first = a.get(20, TimeUnit.SECONDS), second = b.get(20, TimeUnit.SECONDS);
                assertThat(List.of(first.getResponse().getStatus(), second.getResponse().getStatus())).containsExactlyInAnyOrder(201, 409);
                MvcResult loser = first.getResponse().getStatus() == 409 ? first : second;
                assertThat(loser.getResponse().getContentAsString()).contains("VEHICLE_UNAVAILABLE");
            } finally { pool.shutdownNow(); assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue(); }
            assertThat(jdbc.queryForObject("select count(*) from transports where vehicle_id=? and status='IN_TRANSIT'", Long.class, vehicle)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from blood_requests where id in (?,?) and status='IN_TRANSIT'", Long.class, request, otherRequest)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from allocations where id in (?,?) and status='IN_TRANSIT'", Long.class, allocation, otherAllocation)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from allocations where id in (?,?) and status='ALLOCATED'", Long.class, allocation, otherAllocation)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from blood_bags where id in (?,?) and status='IN_TRANSIT'", Long.class, bag, otherBag)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select count(*) from blood_bags where id in (?,?) and status='RESERVED'", Long.class, bag, otherBag)).isEqualTo(1);
        } finally {
            jdbc.update("delete from transports where allocation_id=?", otherAllocation);
            jdbc.update("delete from allocation_items where allocation_id=?", otherAllocation);
            jdbc.update("delete from route_waypoints where route_id=?", otherRoute);
            jdbc.update("delete from delivery_route_nodes where route_id=?", otherRoute);
            jdbc.update("delete from allocations where id=?", otherAllocation);
            jdbc.update("delete from delivery_routes where id=?", otherRoute);
            jdbc.update("delete from blood_requests where id=?", otherRequest);
            jdbc.update("delete from blood_bags where id=?", otherBag);
        }
    }
    @Test @DisplayName("@spec:AC-060 início e cancelamento concorrentes preservam estados coerentes")
    void ac060CoordinatesStartAndCancellation() throws Exception {
        List<Integer> results = race(() -> start().andReturn(), () -> send("/api/v1/requests/" + request + "/cancel", null, access(Role.HOSPITAL, hospital, center), true).andReturn());
        assertThat(results).containsAnyOf(201, 200);
        if (results.get(0) == 201) {
            assertThat(results.get(1)).isEqualTo(422);
            assertStates("IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT", "IN_TRANSIT");
        } else {
            assertThat(results).containsExactly(422, 200);
            assertStates("CANCELLED", "CANCELLED", "RELEASED", "AVAILABLE");
        }
    }
    @Test @DisplayName("@spec:AC-062 entregas concorrentes confirmam uma única vez")
    void ac062CoordinatesConcurrentCompletions() throws Exception {
        start().andExpect(status().isCreated()); UUID id = transport();
        assertThat(race(() -> complete(id).andReturn(), () -> complete(id).andReturn())).containsExactlyInAnyOrder(200, 422);
        assertStates("DELIVERED", "DELIVERED", "DELIVERED", "DELIVERED");
        var completed = jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id);
        complete(id).andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForObject("select completed_at from transports where id=?", java.sql.Timestamp.class, id)).isEqualTo(completed);
    }
    private List<Integer> race(Callable<MvcResult> first, Callable<MvcResult> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try (Connection lock = dataSource.getConnection()) {
            lock.setAutoCommit(false);
            try (var statement = lock.prepareStatement("select id from blood_requests where id=? for update")) {
                statement.setObject(1, request); statement.executeQuery().close();
            }
            CountDownLatch started = new CountDownLatch(2);
            Future<MvcResult> a = pool.submit(() -> { started.countDown(); return first.call(); });
            Future<MvcResult> b = pool.submit(() -> { started.countDown(); return second.call(); });
            assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); int waiting = 0;
            while (System.nanoTime() < until) {
                waiting = jdbc.queryForObject("select count(*) from pg_stat_activity where datname=current_database() and wait_event_type='Lock' and lower(query) like '%blood_requests%'", Integer.class);
                if (waiting >= 2) break;
                Thread.yield();
            }
            assertThat(waiting).isGreaterThanOrEqualTo(2); lock.commit();
            return List.of(a.get(20, TimeUnit.SECONDS).getResponse().getStatus(), b.get(20, TimeUnit.SECONDS).getResponse().getStatus());
        } finally { pool.shutdownNow(); assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue(); }
    }
    private void failOnItemUpdate() {
        trigger = "fail_transport_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("create function " + trigger + "() returns trigger language plpgsql as $$ begin if NEW.blood_bag_id = '" + bag + "'::uuid then raise exception 'fixture failure'; end if; return NEW; end; $$");
        jdbc.execute("create constraint trigger " + trigger + " after update on allocation_items deferrable initially deferred for each row execute function " + trigger + "()");
    }
    private void dropTrigger() { if (trigger != null) { jdbc.execute("drop trigger if exists " + trigger + " on allocation_items"); jdbc.execute("drop function if exists " + trigger + "()"); trigger = null; } }
    private void assertStates(String r, String a, String i, String b) {
        assertThat(jdbc.queryForObject("select status from blood_requests where id=?", String.class, request)).isEqualTo(r);
        assertThat(jdbc.queryForObject("select status from allocations where id=?", String.class, allocation)).isEqualTo(a);
        assertThat(jdbc.queryForObject("select status from allocation_items where allocation_id=?", String.class, allocation)).isEqualTo(i);
        assertThat(jdbc.queryForObject("select status from blood_bags where id=?", String.class, bag)).isEqualTo(b);
    }
    private UUID transport() { return jdbc.queryForObject("select id from transports where allocation_id=?", UUID.class, allocation); }
    private ResultActions start() throws Exception { return send("/api/v1/transports/start", "{\"allocationId\":\"" + allocation + "\",\"vehicleId\":\"" + vehicle + "\"}", access(Role.HEMOCENTRO, hospital, center), true); }
    private ResultActions complete(UUID id) throws Exception { return send("/api/v1/transports/" + id + "/complete", null, access(Role.HOSPITAL, hospital, center), true); }
    private Cookie access(Role role, UUID owner, UUID centerId) { return new Cookie(SessionCookies.ACCESS, issuer.issue(User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, owner, centerId, null)).value()); }
    private ResultActions send(String path, String body, Cookie access, boolean csrf) throws Exception {
        var req = post(path); if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(body);
        if (access != null) req.cookie(access);
        if (csrf) { Cookie token = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN"); req.cookie(token).header("X-XSRF-TOKEN", token.getValue()); }
        return mvc.perform(req);
    }
}
