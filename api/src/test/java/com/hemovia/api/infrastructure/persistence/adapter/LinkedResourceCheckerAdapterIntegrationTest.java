package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.LinkedResourceChecker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Confere se hospital, hemocentro e veículo existem de verdade no PostgreSQL, para validar o vínculo de um usuário.
*/
@Tag("integration")
@SpringBootTest
@Transactional
class LinkedResourceCheckerAdapterIntegrationTest {

    @Autowired
    private LinkedResourceChecker verificador;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void deveEncontrarSomenteOsRecursosQueExistem() {
        // Dados um hospital, um hemocentro e um veículo cadastrados
        UUID hospital = UUID.randomUUID();
        UUID hemocentro = UUID.randomUUID();
        UUID veiculo = UUID.randomUUID();
        jdbc.update("INSERT INTO hospital (id, name, city, latitude, longitude, priority) VALUES (?, 'H', 'Recife', -8.05, -34.9, 3)", hospital);
        jdbc.update("INSERT INTO blood_centers (id, name, city, latitude, longitude) VALUES (?, 'C', 'Recife', -8.06, -34.88)", hemocentro);
        jdbc.update("INSERT INTO vehicles (id, code, capacity) VALUES (?, ?, 4)", veiculo, "VEI-" + veiculo);
        // Então cada um é encontrado pelo seu id
        assertThat(verificador.hospitalExists(hospital)).isTrue();
        assertThat(verificador.bloodCenterExists(hemocentro)).isTrue();
        assertThat(verificador.vehicleExists(veiculo)).isTrue();
        // E um id qualquer não é encontrado em nenhum
        UUID fantasma = UUID.randomUUID();
        assertThat(verificador.hospitalExists(fantasma)).isFalse();
        assertThat(verificador.bloodCenterExists(fantasma)).isFalse();
        assertThat(verificador.vehicleExists(fantasma)).isFalse();
    }
}
