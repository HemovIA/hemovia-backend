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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Tag("integration") @SpringBootTest
class HospitalQueryAcceptanceTest {
    @Autowired WebApplicationContext context;
    @Autowired HospitalRepository hospitals;
    @Autowired AccessTokenIssuer issuer;
    MockMvc mvc; Hospital first,second;
    @BeforeEach void setup() { mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); first=hospital("Hospital Alpha", "Recife"); second=hospital("Beta", "Olinda"); }
    @AfterEach void cleanup() { hospitals.deleteById(first.getId()); hospitals.deleteById(second.getId()); }
    Hospital hospital(String name,String city) { var h=new Hospital(name,city); h.setLatitude(1.0); h.setLongitude(2.0); h.setPriority(3); return hospitals.saveAndFlush(h); }
    Cookie token(Role role,UUID hospital) { var u=User.restore(UUID.randomUUID(),"Pessoa",new Email("teste@example.com"),new Cpf("52998224725"),null,"hash",role,true,hospital,null,null); return new Cookie(SessionCookies.ACCESS,issuer.issue(u).value()); }
    @Test @DisplayName("@spec:AC-083 lista e detalhe retornam dados do hospital")
    void dados() throws Exception {
        Cookie c=token(Role.ADMIN,null);
        mvc.perform(get("/api/v1/hospitals").cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/hospitals/"+first.getId()).cookie(c)).andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(first.getId().toString())).andExpect(jsonPath("$.name").value("Hospital Alpha"))
          .andExpect(jsonPath("$.city").value("Recife")).andExpect(jsonPath("$.latitude").value(1.0)).andExpect(jsonPath("$.longitude").value(2.0)).andExpect(jsonPath("$.priority").value(3));
    }
    @Test @DisplayName("@spec:AC-084 hospitais isolados por vínculo")
    void escopo() throws Exception {
        for(Cookie c:new Cookie[]{token(Role.ADMIN,null),token(Role.HEMOCENTRO,null)}) mvc.perform(get("/api/v1/hospitals").cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        Cookie c=token(Role.HOSPITAL,first.getId());
        mvc.perform(get("/api/v1/hospitals").cookie(c)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(first.getId().toString()));
        mvc.perform(get("/api/v1/hospitals/"+second.getId()).cookie(c)).andExpect(status().isNotFound());
    }
    @Test @DisplayName("@spec:AC-085 transportador não consulta hospitais")
    void negaTransportador() throws Exception {
        Cookie c=token(Role.TRANSPORTADOR,null);
        mvc.perform(get("/api/v1/hospitals").cookie(c)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/hospitals/"+first.getId()).cookie(c)).andExpect(status().isForbidden());
    }
    @Test @DisplayName("@spec:AC-086 trecho sem distinção de caixa e caracteres literais")
    void busca() throws Exception {
        Cookie c=token(Role.ADMIN,null);
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("name","ALpH").param("city","CIF")) .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("name","%")) .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/hospitals").cookie(c).param("city","_")) .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/v1/hospitals").cookie(token(Role.HOSPITAL,second.getId())).param("name","Alpha")) .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }
}
