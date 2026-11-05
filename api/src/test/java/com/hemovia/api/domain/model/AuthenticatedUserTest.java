package com.hemovia.api.domain.model;

import com.hemovia.api.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 6 da Seção 3: o usuário autenticado sabe a que hospital, hemocentro ou veículo pertence.
*/
class AuthenticatedUserTest {

    @Test
    void deveTrabalharSoNoHospitalDoSeuVinculo() {
        // Dado um usuário de hospital
        UUID hospital = UUID.randomUUID();
        AuthenticatedUser usuario = new AuthenticatedUser(UUID.randomUUID(), Role.HOSPITAL, hospital, null, null);
        // Então ele trabalha naquele hospital e em nenhum outro
        assertThat(usuario.worksAtHospital(hospital)).isTrue();
        assertThat(usuario.worksAtHospital(UUID.randomUUID())).isFalse();
    }

    @Test
    void naoDeveTrabalharEmHospitalQuandoOPapelNaoEhHospital() {
        // Mesmo um id de hospital qualquer não dá acesso a quem tem outro papel
        UUID hospital = UUID.randomUUID();
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), Role.ADMIN, hospital, null, null);
        assertThat(admin.worksAtHospital(hospital)).isFalse();
    }

    @Test
    void deveTrabalharSoNoHemocentroDoSeuVinculo() {
        // Dado um usuário de hemocentro
        UUID hemocentro = UUID.randomUUID();
        AuthenticatedUser usuario = new AuthenticatedUser(UUID.randomUUID(), Role.HEMOCENTRO, null, hemocentro, null);
        // Então ele trabalha naquele hemocentro e em nenhum outro
        assertThat(usuario.worksAtBloodCenter(hemocentro)).isTrue();
        assertThat(usuario.worksAtBloodCenter(UUID.randomUUID())).isFalse();
    }

    @Test
    void deveConduzirSoOVeiculoDoSeuVinculo() {
        // Dado um transportador
        UUID veiculo = UUID.randomUUID();
        AuthenticatedUser usuario = new AuthenticatedUser(UUID.randomUUID(), Role.TRANSPORTADOR, null, null, veiculo);
        // Então ele conduz aquele veículo e nenhum outro
        assertThat(usuario.drivesVehicle(veiculo)).isTrue();
        assertThat(usuario.drivesVehicle(UUID.randomUUID())).isFalse();
    }

    @Test
    void naoDeveTerVinculoComIdNulo() {
        // Perguntar por um id nulo nunca dá acesso, nem para quem tem vínculo nulo
        AuthenticatedUser usuario = new AuthenticatedUser(UUID.randomUUID(), Role.HOSPITAL, null, null, null);
        assertThat(usuario.worksAtHospital(null)).isFalse();
    }
}
