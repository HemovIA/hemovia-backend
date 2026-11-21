package com.hemovia.api.controller;

import com.hemovia.api.controller.security.SessionCookies;
import com.hemovia.api.model.User;
import com.hemovia.api.model.entity.Hospital;
import com.hemovia.api.model.enums.Role;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.model.valueobject.Email;
import com.hemovia.api.repository.jpa.HospitalRepository;
import com.hemovia.api.service.port.output.AccessTokenIssuer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration") @SpringBootTest
class TransportQueryAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired HospitalRepository hospitals;
    @Autowired AccessTokenIssuer issuer;
    @Autowired JdbcTemplate jdbc;
    MockMvc mvc;
    Hospital first, second;
    UUID requestA, requestB, center, otherCenter, vehicle, otherVehicle, allocation, allocation2, route, bag, transport;
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        first = hospital("Alpha"); second = hospital("Beta");
        requestA = insert(first.getId(), "A", "HIGH"); requestB = insert(second.getId(), "B", "MEDIUM");
        center=UUID.randomUUID(); otherCenter=UUID.randomUUID(); vehicle=UUID.randomUUID(); otherVehicle=UUID.randomUUID();
        allocation=UUID.randomUUID(); allocation2=UUID.randomUUID(); route=UUID.randomUUID(); bag=UUID.randomUUID(); transport=UUID.randomUUID();
        jdbc.update("insert into blood_centers(id,name,city,latitude,longitude) values (?,'C','C',0,0),(?,'D','C',0,0)", center, otherCenter);
        jdbc.update("insert into vehicles(id,code,capacity) values (?,?,1),(?,?,1)", vehicle, vehicle.toString(), otherVehicle, otherVehicle.toString());
        jdbc.update("insert into delivery_routes(id,origin_id,destination_id,distance_km,estimated_duration_seconds) values (?,?,?,12,60)", route, center, first.getId());
        jdbc.update("insert into route_waypoints(route_id,position,latitude,longitude) values (?,0,1,2)", route);
        jdbc.update("insert into blood_bags(id,blood_center_id,blood_type_enum,rh_factor,component,collection_date,expiration_date,status) values (?,?,'A','POSITIVE','PLATELETS',current_date,current_date,'AVAILABLE')", bag, center);
        jdbc.update("insert into allocations(id,request_id,blood_center_id,status,created_at,updated_at,route_id) values (?,?,?,'CANCELLED',now(),now(),?),(?,?,?,'ALLOCATED',now(),now(),?)", allocation,requestA,center,route,allocation2,requestA,center,route);
        jdbc.update("insert into allocation_items(id,allocation_id,blood_bag_id,status,created_at) values (?,?,?,'RELEASED',now())", UUID.randomUUID(), allocation, bag);
        jdbc.update("insert into transports(id,allocation_id,route_id,vehicle_id,status,started_at) values (?,?,?,?,'DELIVERED',now())", transport,allocation,route,vehicle);
        jdbc.update("update blood_requests set status='ALLOCATED' where id=?",requestA);
    }
    @AfterEach void cleanup() {
        jdbc.update("delete from transports where id=?", transport);
        jdbc.update("delete from allocation_items where allocation_id in (?,?)", allocation, allocation2);
        jdbc.update("delete from allocations where id in (?,?)", allocation, allocation2);
        jdbc.update("delete from route_waypoints where route_id=?", route);
        jdbc.update("delete from delivery_routes where id=?", route);
        jdbc.update("delete from blood_bags where id=?", bag);
        jdbc.update("delete from vehicles where id in (?,?)", vehicle, otherVehicle);
        jdbc.update("delete from blood_centers where id in (?,?)", center, otherCenter);
        jdbc.update("delete from blood_requests where id in (?, ?)", requestA, requestB);
        hospitals.deleteById(first.getId()); hospitals.deleteById(second.getId());
    }
    Hospital hospital(String name) {
        var h = new Hospital(name, "Recife"); h.setLatitude(0.0); h.setLongitude(0.0); h.setPriority(1);
        return hospitals.saveAndFlush(h);
    }
    UUID insert(UUID hospital, String type, String priority) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into blood_requests(id,hospital_id,blood_type,rh_factor,component,quantity,priority,status,created_at,updated_at,version) values (?,?,?,'POSITIVE','PLATELETS',2,?,'PENDING',now(),now(),0)", id, hospital, type, priority);
        return id;
    }
    Cookie token(Role role, UUID hospitalId) { return token(role, hospitalId, null, null); }
    Cookie token(Role role, UUID hospitalId, UUID centerId, UUID vehicleId) {
        var user = User.restore(UUID.randomUUID(), "Pessoa", new Email("teste@example.com"), new Cpf("52998224725"), null, "hash", role, true, hospitalId, centerId, vehicleId);
        return new Cookie(SessionCookies.ACCESS, issuer.issue(user).value());
    }
    @Test @DisplayName("@spec:AC-079 transporte listado com seus vínculos")
    void lista() throws Exception {
        mvc.perform(get("/api/v1/transports").cookie(token(Role.ADMIN,null)))
          .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
          .andExpect(jsonPath("$.content[0].id").value(transport.toString()))
          .andExpect(jsonPath("$.content[0].allocationId").value(allocation.toString()))
          .andExpect(jsonPath("$.content[0].requestId").value(requestA.toString()))
          .andExpect(jsonPath("$.content[0].routeId").value(route.toString()))
          .andExpect(jsonPath("$.content[0].vehicleId").value(vehicle.toString()))
          .andExpect(jsonPath("$.content[0].hospitalId").value(first.getId().toString()))
          .andExpect(jsonPath("$.content[0].bloodCenterId").value(center.toString()))
          .andExpect(jsonPath("$.content[0].status").value("DELIVERED"))
          .andExpect(jsonPath("$.content[0].startedAt").exists());
    }
    @Test @DisplayName("@spec:AC-080 detalhe devolve rota e bolsas persistidas")
    void detalhe() throws Exception {
        mvc.perform(get("/api/v1/transports/"+transport).cookie(token(Role.ADMIN,null)))
          .andExpect(status().isOk()).andExpect(jsonPath("$.transport.id").value(transport.toString()))
          .andExpect(jsonPath("$.route.id").value(route.toString()))
          .andExpect(jsonPath("$.route.coordinates[0][0]").value(2.0))
          .andExpect(jsonPath("$.bloodBagIds[0]").value(bag.toString()));
    }
    @Test @DisplayName("@spec:AC-081 isolamento por perfil e totais")
    void escopo() throws Exception {
        for (Cookie access : new Cookie[]{token(Role.ADMIN,null), token(Role.HOSPITAL,first.getId()), token(Role.HEMOCENTRO,null,center,null), token(Role.TRANSPORTADOR,null,null,vehicle)})
          mvc.perform(get("/api/v1/transports").cookie(access)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        for (Cookie access : new Cookie[]{token(Role.HOSPITAL,second.getId()), token(Role.HEMOCENTRO,null,otherCenter,null), token(Role.TRANSPORTADOR,null,null,otherVehicle)})
          mvc.perform(get("/api/v1/transports").cookie(access)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test @DisplayName("@spec:AC-082 filtros se intersectam com vínculo")
    void filtros() throws Exception {
        Cookie access=token(Role.TRANSPORTADOR,null,null,vehicle);
        mvc.perform(get("/api/v1/transports").cookie(access).param("status","DELIVERED").param("requestId",requestA.toString()).param("vehicleId",vehicle.toString()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/transports").cookie(access).param("vehicleId",otherVehicle.toString()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/transports").cookie(access).param("requestId",requestB.toString()))
          .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/transports").cookie(access).param("status","IN_TRANSIT"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }
}
