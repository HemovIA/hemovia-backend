package com.hemovia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.Person;
import com.hemovia.api.model.DTOs.HospitalResponseDTO;
import com.hemovia.api.model.DTOs.DonationResponseDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.exceptions.GlobalExceptionHandler;
import com.hemovia.api.service.HospitalService;
import com.hemovia.api.service.DonationService;
import com.hemovia.api.service.BloodBagService;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.repository.DonationRepository;
import com.hemovia.api.service.implementation.BloodBagServiceImpl;
import com.hemovia.api.service.implementation.DonationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Constructor;
import java.time.LocalDate;
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
    private final Map<Class<?>, Object> dependencies = new HashMap<>();

    private <T> T service(Class<T> type) throws Exception {
        Constructor<?> ctor = Arrays.stream(type.getConstructors()).max(Comparator.comparingInt(Constructor::getParameterCount)).orElseThrow();
        Object[] args = Arrays.stream(ctor.getParameterTypes()).map(c -> dependencies.computeIfAbsent(c, k -> mock(k))).toArray();
        return type.cast(ctor.newInstance(args));
    }

    private BloodBagRequestDTO legacyRequest(UUID donorId) throws Exception {
        String json = "{\"rhFactor\":\"POSITIVE\",\"component\":\"WHOLE_BLOOD\",\"collectionDate\":\"" + LocalDate.now().minusDays(1)
                + "\",\"expirationDate\":\"" + LocalDate.now().plusDays(30)
                + "\",\"status\":\"AVAILABLE\",\"bloodTypeEnum\":\"O\",\"donorId\":\"" + donorId + "\"}";
        return new ObjectMapper().findAndRegisterModules().readValue(json, BloodBagRequestDTO.class);
    }

    private void donorLookup(Person donor) {
        for (var entry : dependencies.entrySet()) {
            if (entry.getKey().getSimpleName().equals("PersonRepository"))
                when(call(entry.getValue(), "findById", donor.getId())).thenReturn(Optional.of(donor));
        }
    }

    @Test @DisplayName("@spec:AC-015 endpoint hospitalar preserva criação e rejeição de entrada inválida")
    void ac015_hospitalHttpContract() throws Exception {
        HospitalService service = mock(HospitalService.class); var response = new HospitalResponseDTO();
        UUID id = UUID.randomUUID(); response.setId(id); response.setName("Hospital Sintético");
        when(service.create(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new HospitalController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/hospitals").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hospital Sintético\",\"city\":\"Recife\",\"latitude\":0,\"longitude\":0,\"priority\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(id.toString()));
        mvc.perform(post("/hospitals").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"city\":\"Recife\",\"latitude\":91,\"longitude\":0,\"priority\":6}"))
                .andExpect(status().isBadRequest());
        verify(service, times(1)).create(any());
    }

    @Test @DisplayName("@spec:AC-015 endpoint de doação aceita associação singular legada")
    void ac015_donationHttpContract() throws Exception {
        DonationService service = mock(DonationService.class); var response = new DonationResponseDTO();
        UUID donationId = UUID.randomUUID(), bagId = UUID.randomUUID(); response.setDonationId(donationId);
        var bag = new BloodBagResponseDTO(); bag.setBloodBagId(bagId); response.setBloodBag(bag);
        when(service.create(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new DonationController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/donations").contentType(MediaType.APPLICATION_JSON)
                .content("{\"collectionCenter\":\"Centro Sintético\",\"collectionDate\":\"2030-01-01\",\"bloodBagId\":\"" + bagId + "\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.donationId").value(donationId.toString()))
                .andExpect(jsonPath("$.bloodBag.bloodBagId").value(bagId.toString()));
    }

    @Test @DisplayName("@spec:AC-015 endpoint de bolsa aceita o JSON existente")
    void ac015_bloodBagHttpContract() throws Exception {
        BloodBagService service = mock(BloodBagService.class); var response = new BloodBagResponseDTO();
        UUID id = UUID.randomUUID(); response.setBloodBagId(id); when(service.create(any())).thenReturn(response);
        var mvc = MockMvcBuilders.standaloneSetup(new BloodBagController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(legacyRequest(UUID.randomUUID()));
        mvc.perform(post("/blood-bags").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.bloodBagId").value(id.toString()));
    }

    @Test @DisplayName("@spec:AC-015 cadastro legado de bolsa resolve o donorId informado")
    void ac015_legacyBagCreationResolvesDonor() throws Exception {
        var service = service(BloodBagServiceImpl.class);
        var repository = (BloodBagRepository) dependencies.get(BloodBagRepository.class);
        Person donor = new Person(UUID.randomUUID(), "Pessoa Sintética", "donor@example.test", "81000000000"); donorLookup(donor);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(service.create(legacyRequest(donor.getId())));
        var saved = ArgumentCaptor.forClass(BloodBag.class); verify(repository).save(saved.capture());
        assertNotNull(saved.getValue().getDonor(), "donorId não pode ser descartado");
        assertEquals(donor.getId(), saved.getValue().getDonor().getId());
    }

    @Test @DisplayName("@spec:AC-015 doador inexistente não gera bolsa sem rastreabilidade")
    void ac015_unknownDonorDoesNotSave() throws Exception {
        var service = service(BloodBagServiceImpl.class);
        var repository = (BloodBagRepository) dependencies.get(BloodBagRepository.class);
        var request = legacyRequest(UUID.randomUUID());
        assertThrows(EntityNotFoundException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }

    @Test @DisplayName("@spec:AC-015 resposta singular só existe para exatamente uma bolsa")
    void ac015_singleAndMultipleBagResponses() throws Exception {
        var service = service(DonationServiceImpl.class);
        var repository = (DonationRepository) dependencies.get(DonationRepository.class);
        Object d = donation(person(), organization("BloodCenter")); UUID id = UUID.randomUUID(); set(d, "id", id);
        Object first = bag(); set(first, "id", UUID.randomUUID()); attach(d, first);
        when(call(repository, "findById", id)).thenReturn(Optional.of(d));
        Object single = service.getDonationById(id.toString());
        assertNotNull(get(single, "bloodBag")); assertEquals(1, ((Collection<?>) get(single, "bloodBags")).size());
        Object second = bag(); set(second, "id", UUID.randomUUID()); attach(d, second);
        Object multiple = service.getDonationById(id.toString());
        assertNull(get(multiple, "bloodBag"), "Não escolher arbitrariamente uma bolsa da coleção");
        assertEquals(2, ((Collection<?>) get(multiple, "bloodBags")).size());
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(multiple);
        assertTrue(json.contains("bloodBags")); assertFalse(json.contains("password"));
    }

    @Test @DisplayName("@spec:AC-015 data de calendário não muda entre UTC e São Paulo")
    void ac015_civilDateDoesNotShiftByTimezone() throws Exception {
        TimeZone original = TimeZone.getDefault();
        LocalDate expected = LocalDate.now().plusDays(30);
        try {
            for (String zone : List.of("UTC", "America/Sao_Paulo")) {
                TimeZone.setDefault(TimeZone.getTimeZone(zone));
                var service = service(BloodBagServiceImpl.class);
                var repository = (BloodBagRepository) dependencies.get(BloodBagRepository.class);
                Person donor = new Person(UUID.randomUUID(), "Pessoa Sintética", "donor@example.test", "81000000000"); donorLookup(donor);
                when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
                var response = service.create(legacyRequest(donor.getId()));
                assertEquals(expected.toString(), response.getExpirationDate(), "Data civil deve usar ISO sem mudar de dia: " + zone);
            }
        } finally { TimeZone.setDefault(original); }
    }
}
