package com.hemovia.api.integration;

import com.hemovia.api.model.Hospital;
import com.hemovia.api.repository.HospitalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração com o PostgreSQL real: valida que a migration V3 (Flyway)
 * e o mapeamento JPA da entidade Hospital funcionam juntos.
 */
@Tag("integration")
@SpringBootTest
class HospitalRepositoryIntegrationTest {

    @Autowired
    private HospitalRepository repository;

    @Test
    @DisplayName("Salva e recupera um hospital no PostgreSQL")
    void shouldPersistAndReadHospital() {
        Hospital hospital = new Hospital();
        hospital.setName("Hospital Sintético da Restauração");
        hospital.setCity("Recife");
        hospital.setLatitude(-8.0476);
        hospital.setLongitude(-34.8770);
        hospital.setPriority(1);

        Hospital saved = repository.save(hospital);

        assertThat(saved.getId()).isNotNull();
        Hospital found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Hospital Sintético da Restauração");
        assertThat(found.getLatitude()).isEqualTo(-8.0476);
        assertThat(found.getPriority()).isEqualTo(1);
    }
}
