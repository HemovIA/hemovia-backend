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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
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
class DeliveryRouteAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccessTokenIssuer issuer;
    @Autowired ClockProvider clock;
    MockMvc mvc;
    UUID center, hospital, origin, destination, request, bag, junction;
    String trigger, function;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        center = UUID.randomUUID(); hospital = UUID.randomUUID(); request = UUID.randomUUID(); bag = UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?,'C','C',0,0)", center);
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?,'H','C',0,1,1)", hospital);
        origin = RouteGraphFixtures.center(jdbc, center); destination = RouteGraphFixtures.hospital(jdbc, hospital);
        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',?,?,'AVAILABLE')", bag, center, today.minusDays(1), today.plusDays(5));
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,status,created_at,updated_at,priority,delivery_deadline) values (?,?,'O','POSITIVE','RED_BLOOD_CELLS',1,'PENDING',now(),now(),'HIGH',?)", request, hospital, java.sql.Timestamp.from(clock.now().plusSeconds(86400)));
    }

    @AfterEach void cleanup() {
        dropTrigger();
        jdbc.update("delete from allocation_items where allocation_id in (select id from allocations where request_id=?)", request);
        var routes = jdbc.queryForList("select route_id from allocations where request_id=? and route_id is not null", UUID.class, request);
        for (UUID route : routes) {
            jdbc.update("delete from route_waypoints where route_id=?", route);
            jdbc.update("delete from delivery_route_nodes where route_id=?", route);
        }
        jdbc.update("delete from allocations where request_id=?", request);
        for (UUID route : routes) jdbc.update("delete from delivery_routes where id=?", route);
        jdbc.update("delete from blood_requests where id=?", request);
        jdbc.update("delete from blood_bags where id=?", bag);
        if (junction != null) RouteGraphFixtures.cleanup(jdbc, junction);
        RouteGraphFixtures.cleanup(jdbc, origin, destination);
        jdbc.update("delete from blood_centers where id=?", center);
        jdbc.update("delete from hospital where id=?", hospital);
    }

    @Test @DisplayName("@spec:AC-052 alocação persiste rota e geometria ordenadas")
    void ac052PersistsRouteSnapshot() throws Exception {
        junction = RouteGraphFixtures.junction(jdbc, 0, 0.5);
        RouteGraphFixtures.edge(jdbc, origin, destination, 200);
        RouteGraphFixtures.edge(jdbc, origin, junction, 40);
        RouteGraphFixtures.edge(jdbc, junction, destination, 60);
        allocate().andExpect(status().isCreated()).andExpect(jsonPath("$.route.nodeIds[0]").value(origin.toString()))
                .andExpect(jsonPath("$.route.nodeIds[1]").value(junction.toString()))
                .andExpect(jsonPath("$.route.nodeIds[2]").value(destination.toString()))
                .andExpect(jsonPath("$.route.estimatedDurationSeconds").value(100));
        UUID route = jdbc.queryForObject("select route_id from allocations where request_id=?", UUID.class, request);
        assertThat(route).isNotNull();
        assertThat(jdbc.queryForList("select node_id from delivery_route_nodes where route_id=? order by position", UUID.class, route)).containsExactly(origin, junction, destination);
        assertThat(jdbc.queryForList("select longitude from route_waypoints where route_id=? order by position", Double.class, route)).containsExactly(0.0, 0.5, 1.0);
    }

    @Test @DisplayName("@spec:AC-053 rota ausente ou prazo impossível desfaz reserva")
    void ac053RollsBackMissingRouteAndDeadline() throws Exception {
        jdbc.update("delete from route_nodes where id=?", destination);
        allocate().andExpect(status().isUnprocessableEntity()); assertUnchanged();
        destination = RouteGraphFixtures.hospital(jdbc, hospital);
        allocate().andExpect(status().isUnprocessableEntity()); assertUnchanged();
        RouteGraphFixtures.edge(jdbc, origin, destination, 60);
        jdbc.update("update blood_requests set delivery_deadline=? where id=?", java.sql.Timestamp.from(clock.now().minusSeconds(1)), request);
        allocate().andExpect(status().isUnprocessableEntity()); assertUnchanged();
    }

    @Test @DisplayName("@spec:AC-054 prévia respeita prazo, plaquetas e segurança sem gravar")
    void ac054PreviewChecksWindowAndSecurity() throws Exception {
        RouteGraphFixtures.edge(jdbc, origin, destination, 5100);
        preview("PLATELETS", 10000, access(Role.HEMOCENTRO), true).andExpect(status().isUnprocessableEntity());
        jdbc.update("update route_edges set travel_seconds=3600 where from_node_id=? and to_node_id=?", origin, destination);
        preview("PLATELETS", 10000, access(Role.HEMOCENTRO), true).andExpect(status().isOk()).andExpect(jsonPath("$.estimatedDurationSeconds").value(3600));
        preview("PLATELETS", 1, access(Role.HEMOCENTRO), true).andExpect(status().isUnprocessableEntity());
        preview("PLATELETS", 10000, null, true).andExpect(status().isUnauthorized());
        preview("PLATELETS", 10000, access(Role.ADMIN), true).andExpect(status().isForbidden());
        preview("PLATELETS", 10000, access(Role.HEMOCENTRO), false).andExpect(status().isForbidden());
        assertUnchanged();
    }

    @Test @DisplayName("@spec:AC-055 falha tardia da rota desfaz reserva e permite repetição")
    void ac055RollsBackRouteCommitFailureAndRetries() throws Exception {
        RouteGraphFixtures.edge(jdbc, origin, destination, 60);
        function = "fail_route_" + UUID.randomUUID().toString().replace("-", ""); trigger = function;
        jdbc.execute("create function " + function + "() returns trigger language plpgsql as $$ begin if NEW.origin_id = '" + center + "'::uuid then raise exception 'fixture failure'; end if; return NEW; end; $$");
        jdbc.execute("create constraint trigger " + trigger + " after insert on delivery_routes deferrable initially deferred for each row execute function " + function + "()");
        assertThat(jdbc.queryForObject("select count(*) from delivery_route_nodes n join delivery_routes r on r.id=n.route_id where r.origin_id=?", Long.class, center)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from route_waypoints w join delivery_routes r on r.id=w.route_id where r.origin_id=?", Long.class, center)).isZero();
        long nodesBefore = jdbc.queryForObject("select count(*) from delivery_route_nodes", Long.class);
        long waypointsBefore = jdbc.queryForObject("select count(*) from route_waypoints", Long.class);
        allocate().andExpect(status().isInternalServerError()); assertUnchanged();
        assertThat(jdbc.queryForObject("select count(*) from delivery_route_nodes", Long.class)).isEqualTo(nodesBefore);
        assertThat(jdbc.queryForObject("select count(*) from route_waypoints", Long.class)).isEqualTo(waypointsBefore);
        assertThat(jdbc.queryForObject("select count(*) from delivery_route_nodes n join delivery_routes r on r.id=n.route_id where r.origin_id=?", Long.class, center)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from route_waypoints w join delivery_routes r on r.id=w.route_id where r.origin_id=?", Long.class, center)).isZero();
        dropTrigger(); allocate().andExpect(status().isCreated());
        UUID route = jdbc.queryForObject("select route_id from allocations where request_id=?", UUID.class, request);
        assertThat(route).isNotNull();
        assertThat(jdbc.queryForList("select node_id from delivery_route_nodes where route_id=? order by position", UUID.class, route)).containsExactly(origin, destination);
        assertThat(jdbc.queryForList("select longitude from route_waypoints where route_id=? order by position", Double.class, route)).containsExactly(0.0, 1.0);
        assertThat(jdbc.queryForObject("select count(*) from delivery_route_nodes", Long.class)).isEqualTo(nodesBefore + 2);
        assertThat(jdbc.queryForObject("select count(*) from route_waypoints", Long.class)).isEqualTo(waypointsBefore + 2);
    }

    private void assertUnchanged() {
        assertThat(jdbc.queryForObject("select status from blood_requests where id=?", String.class, request)).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("select status from blood_bags where id=?", String.class, bag)).isEqualTo("AVAILABLE");
        assertThat(jdbc.queryForObject("select count(*) from allocations where request_id=?", Long.class, request)).isZero();
        assertThat(jdbc.queryForObject("select count(route_id) from allocations where request_id=?", Long.class, request)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from allocation_items where blood_bag_id=?", Long.class, bag)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from delivery_routes where origin_id=?", Long.class, center)).isZero();
    }
    private void dropTrigger() {
        if (trigger != null) { jdbc.execute("drop trigger if exists " + trigger + " on delivery_routes"); trigger = null; }
        if (function != null) { jdbc.execute("drop function if exists " + function + "()"); function = null; }
    }
    private Cookie access(Role role) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, null, center, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private ResultActions allocate() throws Exception {
        Cookie csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        return mvc.perform(post("/api/v1/requests/{id}/allocate", request).cookie(access(Role.HEMOCENTRO), csrf).header("X-XSRF-TOKEN", csrf.getValue()));
    }
    private ResultActions preview(String component, long seconds, Cookie access, boolean useCsrf) throws Exception {
        var action = post("/api/v1/routes/calculate").contentType(MediaType.APPLICATION_JSON).content("{\"hospitalId\":\"" + hospital + "\",\"component\":\"" + component + "\",\"deliveryDeadline\":\"" + clock.now().plusSeconds(seconds) + "\"}");
        if (access != null) action.cookie(access);
        if (useCsrf) { Cookie csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN"); action.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()); }
        return mvc.perform(action);
    }
}
