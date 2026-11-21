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
class OperationalQueriesContractTest {
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
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.hemovia.api.service.port.output.RoadRoutingProvider routing;

    @Test @DisplayName("@spec:AC-087 as seis consultas exigem sessão")
    void exigeSessao() throws Exception {
        for (String path : new String[]{"/api/v1/requests", "/api/v1/requests/"+requestA, "/api/v1/transports", "/api/v1/transports/"+transport, "/api/v1/hospitals", "/api/v1/hospitals/"+first.getId()})
            mvc.perform(get(path)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }
    @Test @DisplayName("@spec:AC-088 detalhes de outros vínculos são ocultos")
    void oculta() throws Exception {
        for (String path : new String[]{"/api/v1/requests/"+requestA,"/api/v1/transports/"+transport,"/api/v1/hospitals/"+first.getId()}) {
            mvc.perform(get(path).cookie(token(Role.HOSPITAL,second.getId()))).andExpect(status().isNotFound());
            mvc.perform(get(path).cookie(token(Role.HOSPITAL,null))).andExpect(status().isNotFound());
            mvc.perform(get(path.replaceFirst("[^/]+$",UUID.randomUUID().toString())).cookie(token(Role.ADMIN,null))).andExpect(status().isNotFound());
        }
        for (Cookie c : new Cookie[]{token(Role.HEMOCENTRO,null,otherCenter,null), token(Role.TRANSPORTADOR,null,null,otherVehicle)}) {
            mvc.perform(get("/api/v1/requests/"+requestA).cookie(c)).andExpect(status().isNotFound());
            mvc.perform(get("/api/v1/transports/"+transport).cookie(c)).andExpect(status().isNotFound());
        }
    }
    @Test @DisplayName("@spec:AC-089 parâmetros inválidos geram ApiError")
    void invalido() throws Exception {
        Cookie c=token(Role.ADMIN,null);
        for (String path : new String[]{"/api/v1/requests", "/api/v1/transports", "/api/v1/hospitals"}) {
            for (String[] param : new String[][]{{"page","-1"},{"page","abc"},{"size","0"},{"size","101"},{"size","x"}})
                mvc.perform(get(path).cookie(c).param(param[0],param[1])).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists()).andExpect(jsonPath("$.status").value(400));
            mvc.perform(get(path+"/not-a-uuid").cookie(c)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
        }
        for (String[] v : new String[][]{{"/api/v1/requests","hospitalId"},{"/api/v1/requests","status"},{"/api/v1/requests","bloodType"},{"/api/v1/requests","rhFactor"},{"/api/v1/requests","component"},{"/api/v1/requests","priority"},{"/api/v1/transports","status"},{"/api/v1/transports","requestId"},{"/api/v1/transports","vehicleId"}})
            mvc.perform(get(v[0]).cookie(c).param(v[1],"invalid")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }
    @Test @DisplayName("@spec:AC-090 paginação, empates e deslocamentos grandes")
    void pagina() throws Exception {
        Cookie c=token(Role.ADMIN,null);
        jdbc.update("update blood_requests set created_at='2025-01-01T00:00:00Z' where id in (?,?)",requestA,requestB);
        String firstId=requestA.toString().compareTo(requestB.toString())<0?requestA.toString():requestB.toString();
        mvc.perform(get("/api/v1/requests").cookie(c).param("size","1"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(firstId)).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(1)).andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
        String secondId=firstId.equals(requestA.toString())?requestB.toString():requestA.toString();
        mvc.perform(get("/api/v1/requests").cookie(c).param("size","1").param("page","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(secondId));
        jdbc.update("update blood_requests set created_at='2025-01-02T00:00:00Z' where id=?",requestB);
        mvc.perform(get("/api/v1/requests").cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(requestB.toString()));
        String city="cidade-"+UUID.randomUUID();
        jdbc.update("update hospital set name='Mesmo', city=? where id in (?,?)",city,first.getId(),second.getId());
        String h1=first.getId().toString().compareTo(second.getId().toString())<0?first.getId().toString():second.getId().toString();
        String h2=h1.equals(first.getId().toString())?second.getId().toString():first.getId().toString();
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("city",city).param("size","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(h1));
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("city",city).param("size","1").param("page","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(h2));
        jdbc.update("update hospital set name='A' where id=?",second.getId());
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("city",city)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(second.getId().toString()));
        UUID t2=UUID.randomUUID();
        jdbc.update("update transports set started_at='2025-01-01T00:00:00Z' where id=?",transport);
        jdbc.update("insert into transports(id,allocation_id,route_id,vehicle_id,status,started_at) values (?,?,?,?,'DELIVERED','2025-01-01T00:00:00Z')",t2,allocation2,route,otherVehicle);
        try {
            String t1=transport.toString().compareTo(t2.toString())<0?transport.toString():t2.toString();
            String tLast=t1.equals(transport.toString())?t2.toString():transport.toString();
            mvc.perform(get("/api/v1/transports").cookie(c).param("size","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(t1));
            mvc.perform(get("/api/v1/transports").cookie(c).param("size","1").param("page","1")).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(tLast));
            jdbc.update("update transports set started_at='2025-01-02T00:00:00Z' where id=?",t2);
            mvc.perform(get("/api/v1/transports").cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(t2.toString()));
        } finally { jdbc.update("delete from transports where id=?",t2); }
        for(String path:new String[]{"/api/v1/requests","/api/v1/transports","/api/v1/hospitals"}) {
            mvc.perform(get(path).cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(20));
            mvc.perform(get(path).cookie(c).param("page","2147483647").param("size","100"))
              .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0)).andExpect(jsonPath("$.totalElements").value(path.endsWith("transports")?1:path.endsWith("hospitals")?(int) hospitals.count():2));
        }
    }
    @Test @DisplayName("@spec:AC-091 filtro sem resultado e vínculo ausente não expõem registros")
    void vazio() throws Exception {
        for (String path : new String[]{"/api/v1/requests","/api/v1/transports","/api/v1/hospitals"})
            mvc.perform(get(path).cookie(token(Role.HOSPITAL,null))).andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/hospitals").cookie(token(Role.ADMIN,null)).param("name","inexistente"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(0)).andExpect(jsonPath("$.totalElements").value(0));
    }
    @Test @DisplayName("@spec:AC-093 consultas não modificam estado nem chamam roteador")
    void leitura() throws Exception {
        var tables=java.util.List.of("blood_requests","allocations","allocation_items","blood_bags","delivery_routes","route_waypoints","delivery_route_nodes","transports");
        var before=new java.util.LinkedHashMap<String,java.util.List<String>>();
        for(String table:tables) before.put(table,jdbc.queryForList("select row_to_json(t)::text from "+table+" t order by 1",String.class));
        Cookie c=token(Role.ADMIN,null);
        for (String path : new String[]{"/api/v1/requests","/api/v1/requests/"+requestA,"/api/v1/transports","/api/v1/transports/"+transport,"/api/v1/hospitals","/api/v1/hospitals/"+first.getId()})
            mvc.perform(get(path).cookie(c)).andExpect(status().isOk());
        for(String table:tables) org.assertj.core.api.Assertions.assertThat(jdbc.queryForList("select row_to_json(t)::text from "+table+" t order by 1",String.class)).isEqualTo(before.get(table));
        org.mockito.Mockito.verifyNoInteractions(routing);
    }
}
