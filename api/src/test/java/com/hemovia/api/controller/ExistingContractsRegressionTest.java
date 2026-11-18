package com.hemovia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.dto.request.BloodBagRequest;
import com.hemovia.api.model.entity.Person;
import com.hemovia.api.dto.response.HospitalResponse;
import com.hemovia.api.dto.response.DonationResponse;
import com.hemovia.api.dto.response.BloodBagResponse;
import com.hemovia.api.controller.handler.GlobalExceptionHandler;
import com.hemovia.api.service.config.SystemClockProvider;
import java.time.Clock;
import com.hemovia.api.service.usecase.hospital.CreateHospital;
import com.hemovia.api.service.usecase.inventory.InventoryLinks;
import com.hemovia.api.service.usecase.inventory.ManageDonations;
import com.hemovia.api.service.usecase.inventory.ManageBloodBags;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import com.hemovia.api.repository.jpa.DonationRepository;
import com.hemovia.api.repository.jpa.BloodCenterRepository;
import com.hemovia.api.repository.jpa.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static com.hemovia.api.support.BaseEntityFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

class ExistingContractsRegressionTest {
    private final BloodBagRepository bags = mock(BloodBagRepository.class);
    private final DonationRepository donations = mock(DonationRepository.class);
    private final PersonRepository people = mock(PersonRepository.class);
    private final InventoryLinks links = new InventoryLinks(people, mock(BloodCenterRepository.class));

    private BloodBagRequest legacyRequest(UUID donorId) throws Exception {
        return legacyRequest(donorId, LocalDate.now(ZoneOffset.UTC));
    }

    private BloodBagRequest legacyRequest(UUID donorId, LocalDate referenceDate) throws Exception {
        String json = "{\"rhFactor\":\"POSITIVE\",\"component\":\"WHOLE_BLOOD\",\"collectionDate\":\"" + referenceDate.minusDays(1)
                + "\",\"expirationDate\":\"" + referenceDate.plusDays(30)
                + "\",\"status\":\"AVAILABLE\",\"bloodTypeEnum\":\"O\",\"donorId\":\"" + donorId + "\"}";
        return new ObjectMapper().findAndRegisterModules().readValue(json, BloodBagRequest.class);
    }

    private void donorLookup(Person donor) {
        when(people.findById(donor.getId())).thenReturn(Optional.of(donor));
    }

    @Test @DisplayName("@spec:AC-015 endpoint hospitalar preserva criação e rejeição de entrada inválida")
    void ac015_hospitalHttpContract() throws Exception {
        CreateHospital service = mock(CreateHospital.class);
        UUID id = UUID.randomUUID();
        var response = new HospitalResponse(id, "Hospital Sintético", "Recife", 0, 0, 1);
        when(service.execute(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new HospitalController(service)).setControllerAdvice(new GlobalExceptionHandler(new SystemClockProvider(Clock.systemUTC()))).build();
        mvc.perform(post("/hospitals").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hospital Sintético\",\"city\":\"Recife\",\"latitude\":0,\"longitude\":0,\"priority\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()));
        mvc.perform(post("/hospitals").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"city\":\"Recife\",\"latitude\":91,\"longitude\":0,\"priority\":6}"))
                .andExpect(status().isBadRequest());
        verify(service, times(1)).execute(any());
    }

    @Test @DisplayName("@spec:AC-015 endpoint de doação aceita associação singular legada")
    void ac015_donationHttpContract() throws Exception {
        ManageDonations service = mock(ManageDonations.class);
        UUID donationId = UUID.randomUUID(), bagId = UUID.randomUUID();
        var bag = new BloodBagResponse(bagId, null, null, null, null, null, null, null, null);
        var response = new DonationResponse(donationId, null, null, bag, List.of(bag), null, null);
        when(service.create(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new DonationController(service)).setControllerAdvice(new GlobalExceptionHandler(new SystemClockProvider(Clock.systemUTC()))).build();
        mvc.perform(post("/donations").contentType(MediaType.APPLICATION_JSON)
                .content("{\"collectionCenter\":\"Centro Sintético\",\"collectionDate\":\"2030-01-01\",\"bloodBagId\":\"" + bagId + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.donationId").value(donationId.toString()))
                .andExpect(jsonPath("$.bloodBag.bloodBagId").value(bagId.toString()));
    }

    @Test @DisplayName("@spec:AC-015 endpoint de bolsa aceita o JSON existente")
    void ac015_bloodBagHttpContract() throws Exception {
        ManageBloodBags service = mock(ManageBloodBags.class);
        UUID id = UUID.randomUUID();
        when(service.create(any())).thenReturn(new BloodBagResponse(id, null, null, null, null, null, null, null, null));
        var mvc = MockMvcBuilders.standaloneSetup(new BloodBagController(service)).setControllerAdvice(new GlobalExceptionHandler(new SystemClockProvider(Clock.systemUTC()))).build();
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(legacyRequest(UUID.randomUUID()));
        mvc.perform(post("/blood-bags").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.bloodBagId").value(id.toString()));
    }

    @Test @DisplayName("@spec:AC-015 cadastro legado de bolsa resolve o donorId informado")
    void ac015_legacyBagCreationResolvesDonor() throws Exception {
        var service = new ManageBloodBags(bags, links);
        var repository = bags;
        Person donor = new Person(UUID.randomUUID(), "Pessoa Sintética", "donor@example.test", "81000000000"); donorLookup(donor);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(service.create(legacyRequest(donor.getId())));
        var saved = ArgumentCaptor.forClass(BloodBag.class); verify(repository).save(saved.capture());
        assertNotNull(saved.getValue().getDonor(), "donorId não pode ser descartado");
        assertEquals(donor.getId(), saved.getValue().getDonor().getId());
    }

    @Test @DisplayName("@spec:AC-015 doador inexistente não gera bolsa sem rastreabilidade")
    void ac015_unknownDonorDoesNotSave() throws Exception {
        var service = new ManageBloodBags(bags, links);
        var repository = bags;
        var request = legacyRequest(UUID.randomUUID());
        assertThrows(EntityNotFoundException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test @DisplayName("@spec:AC-015 resposta singular só existe para exatamente uma bolsa")
    void ac015_singleAndMultipleBagResponses() throws Exception {
        var service = new ManageDonations(donations, bags, links);
        var repository = donations;
        Object d = donation(person(), organization("BloodCenter")); UUID id = UUID.randomUUID(); set(d, "id", id);
        Object first = bag(); set(first, "id", UUID.randomUUID()); attach(d, first);
        when(call(repository, "findById", id)).thenReturn(Optional.of(d));
        var single = service.get(id.toString());
        assertNotNull(single.bloodBag()); assertEquals(1, single.bloodBags().size());
        Object second = bag(); set(second, "id", UUID.randomUUID()); attach(d, second);
        var multiple = service.get(id.toString());
        assertNull(multiple.bloodBag(), "Não escolher arbitrariamente uma bolsa da coleção");
        assertEquals(2, multiple.bloodBags().size());
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(multiple);
        assertTrue(json.contains("bloodBags")); assertFalse(json.contains("password"));
    }

    @Test @DisplayName("@spec:AC-015 data de calendário não muda entre UTC e São Paulo")
    void ac015_civilDateDoesNotShiftByTimezone() throws Exception {
        TimeZone original = TimeZone.getDefault();
        LocalDate referenceDate = LocalDate.now(ZoneOffset.UTC);
        LocalDate expected = referenceDate.plusDays(30);
        try {
            for (String zone : List.of("UTC", "America/Sao_Paulo")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                var service = new ManageBloodBags(bags, links);
                var repository = bags;
                Person donor = new Person(UUID.randomUUID(), "Pessoa Sintética", "donor@example.test", "81000000000"); donorLookup(donor);
                when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
                var response = service.create(legacyRequest(donor.getId(), referenceDate));
                assertEquals(expected.toString(), response.expirationDate(), "Data civil deve usar ISO sem mudar de dia: " + zone);
            }
        } finally { TimeZone.setDefault(original); }
    }
}
