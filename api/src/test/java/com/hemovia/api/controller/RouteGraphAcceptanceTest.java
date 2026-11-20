package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.RoadSegment;
import com.hemovia.api.model.User;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.exception.RoutingProviderUnavailableException;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.model.valueobject.GeoPoint;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import com.hemovia.api.service.port.output.RoadRoutingProvider;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
class RouteGraphAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired AccessTokenIssuer issuer;
    @MockitoBean RoadRoutingProvider provider;
    MockMvc mvc;
    UUID center, hospital;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        center = UUID.randomUUID(); hospital = UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?,'C','C',-8,-34)", center);
        jdbc.update("insert into hospital(id,name,city,latitude,longitude,priority) values (?,'H','C',-9,-35,1)", hospital);
    }
    @AfterEach void cleanup() {
        jdbc.update("delete from route_edges where from_node_id in (select id from route_nodes where blood_center_id=? or hospital_id=?) or to_node_id in (select id from route_nodes where blood_center_id=? or hospital_id=?)", center, hospital, center, hospital);
        jdbc.update("delete from route_nodes where blood_center_id=? or hospital_id=?", center, hospital);
        jdbc.update("delete from blood_centers where id=?", center);
        jdbc.update("delete from hospital where id=?", hospital);
    }

    @Test @DisplayName("@spec:AC-051 cadastro direcional deriva coordenadas e protege a API")
    void ac051ManagesGraphWithSecurityAndValidation() throws Exception {
        String c = "{\"name\":\"C\",\"bloodCenterId\":\"" + center + "\"}";
        String h = "{\"name\":\"H\",\"hospitalId\":\"" + hospital + "\"}";
        write("nodes", c, null, true).andExpect(status().isUnauthorized());
        write("nodes", c, access(Role.HEMOCENTRO), true).andExpect(status().isForbidden());
        write("nodes", c, access(Role.ADMIN), false).andExpect(status().isForbidden());
        write("nodes", c, access(Role.ADMIN), true).andExpect(status().isCreated()).andExpect(jsonPath("$.latitude").value(-8));
        write("nodes", h, access(Role.ADMIN), true).andExpect(status().isCreated()).andExpect(jsonPath("$.longitude").value(-35));
        UUID deletedCenter = UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude,deleted_at) values (?,'Deleted','C',0,0,now())", deletedCenter);
        try {
            write("nodes", "{\"name\":\"deleted\",\"bloodCenterId\":\"" + deletedCenter + "\"}", access(Role.ADMIN), true)
                    .andExpect(status().isNotFound());
            assertThat(jdbc.queryForObject("select count(*) from route_nodes where blood_center_id=?", Long.class, deletedCenter)).isZero();
            verifyNoInteractions(provider);
        } finally {
            jdbc.update("delete from blood_centers where id=?", deletedCenter);
        }
        write("nodes", c, access(Role.ADMIN), true).andExpect(status().isConflict());
        write("nodes", "{\"name\":\"bad\",\"latitude\":91,\"longitude\":0}", access(Role.ADMIN), true).andExpect(status().isBadRequest());
        write("nodes", "{\"name\":\"bad\",\"hospitalId\":\"" + UUID.randomUUID() + "\"}", access(Role.ADMIN), true).andExpect(status().isNotFound());
        write("nodes", "{\"name\":\"bad\",\"bloodCenterId\":\"" + UUID.randomUUID() + "\"}", access(Role.ADMIN), true).andExpect(status().isNotFound());
        write("nodes", "{\"name\":\"bad\",\"bloodCenterId\":\"" + center + "\",\"latitude\":0,\"longitude\":0}", access(Role.ADMIN), true).andExpect(status().isBadRequest());
        UUID from = jdbc.queryForObject("select id from route_nodes where blood_center_id=?", UUID.class, center);
        UUID to = jdbc.queryForObject("select id from route_nodes where hospital_id=?", UUID.class, hospital);
        String edge = "{\"fromNodeId\":\"" + from + "\",\"toNodeId\":\"" + to + "\"}";
        when(provider.route(any(), any())).thenReturn(new RoadSegment(BigDecimal.ONE, 60, List.of(new GeoPoint(-8.0,-34.0), new GeoPoint(-9.0,-35.0))));
        write("edges", edge, access(Role.ADMIN), true).andExpect(status().isCreated()).andExpect(jsonPath("$.geometry.coordinates[0][0]").value(-34));
        write("edges", edge, access(Role.ADMIN), true).andExpect(status().isConflict());
        write("edges", "{\"fromNodeId\":\"" + from + "\",\"toNodeId\":\"" + from + "\"}", access(Role.ADMIN), true).andExpect(status().isBadRequest());
        write("edges", "{\"fromNodeId\":\"" + from + "\",\"toNodeId\":\"" + UUID.randomUUID() + "\"}", access(Role.ADMIN), true).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/route-graph/nodes").cookie(access(Role.HEMOCENTRO))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/route-graph/edges").cookie(access(Role.HOSPITAL))).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select count(*) from route_edges where from_node_id=? and to_node_id=? and bidirectional=false", Long.class, from, to)).isEqualTo(1);
        jdbc.update("update blood_centers set deleted_at=now() where id=?", center);
        String reverse = "{\"fromNodeId\":\"" + to + "\",\"toNodeId\":\"" + from + "\"}";
        write("edges", reverse, access(Role.ADMIN), true).andExpect(status().isNotFound());
    }

    @Test @DisplayName("@spec:AC-051 falhas do provedor não persistem ligação")
    void ac051ProviderFailuresDoNotPersistEdges() throws Exception {
        write("nodes", "{\"name\":\"C\",\"bloodCenterId\":\"" + center + "\"}", access(Role.ADMIN), true).andExpect(status().isCreated());
        write("nodes", "{\"name\":\"H\",\"hospitalId\":\"" + hospital + "\"}", access(Role.ADMIN), true).andExpect(status().isCreated());
        UUID from = jdbc.queryForObject("select id from route_nodes where blood_center_id=?", UUID.class, center);
        UUID to = jdbc.queryForObject("select id from route_nodes where hospital_id=?", UUID.class, hospital);
        String edge = "{\"fromNodeId\":\"" + from + "\",\"toNodeId\":\"" + to + "\"}";
        when(provider.route(any(), any())).thenThrow(new InvalidRouteException("sem rota"), new RoutingProviderUnavailableException("indisponível"));
        write("edges", edge, access(Role.ADMIN), true).andExpect(status().isUnprocessableEntity());
        write("edges", edge, access(Role.ADMIN), true).andExpect(status().isServiceUnavailable());
        assertThat(jdbc.queryForObject("select count(*) from route_edges where from_node_id=?", Long.class, from)).isZero();
    }
    private Cookie access(Role role) {
        User user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, hospital, center, null);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    private ResultActions write(String resource, String body, Cookie access, boolean useCsrf) throws Exception {
        var action = post("/api/v1/route-graph/" + resource).contentType(MediaType.APPLICATION_JSON).content(body);
        if (access != null) action.cookie(access);
        if (useCsrf) { Cookie csrf = mvc.perform(get("/api/v1/auth/csrf")).andReturn().getResponse().getCookie("XSRF-TOKEN"); action.cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()); }
        return mvc.perform(action);
    }
}
