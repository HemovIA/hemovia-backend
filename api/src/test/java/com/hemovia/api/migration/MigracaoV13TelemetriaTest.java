package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/*
Ciclo 2 da Seção 2, migration V13: posição (latitude e longitude) em cada leitura de telemetria.
As leituras antigas ficam sem posição, e as novas só aceitam coordenadas que existem no planeta.
Roda contra um PostgreSQL real, num schema isolado, até a V13.
*/
@Tag("integration")
class MigracaoV13TelemetriaTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    // Transporte a que as leituras pertencem
    private UUID transporte;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com todas as migrations aplicadas e um transporte para receber leituras
        banco = new BancoDeMigracao();
        banco.migrarAte("13");
        transporte = banco.criarTransporte();
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    // Insere uma leitura com a posição informada (nula significa sem posição)
    private void inserirLeitura(Double latitude, Double longitude) throws SQLException {
        banco.executar("INSERT INTO telemetry_readings (id, transport_id, sensor_id, measured_at, received_at, temperature_celsius, latitude, longitude) "
                + "VALUES (?, ?, 'SENSOR-1', TIMESTAMPTZ '2026-08-11 12:00:00+00', TIMESTAMPTZ '2026-08-11 12:00:01+00', 4.5, ?, ?)",
                UUID.randomUUID(), transporte, latitude, longitude);
    }

    @Test
    void deveAceitarLeituraSemPosicao() {
        // Leituras no formato antigo não têm posição, e continuam válidas
        assertThatCode(() -> inserirLeitura(null, null)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarLeituraComPosicaoValida() throws SQLException {
        // Quando gravamos uma posição em Recife
        inserirLeitura(-8.05, -34.90);
        // Então ela fica guardada
        assertThat(banco.contar("SELECT count(*) FROM telemetry_readings WHERE latitude = -8.05 AND longitude = -34.90")).isEqualTo(1);
    }

    @Test
    void deveRejeitarLatitudeForaDaFaixa() {
        // Latitude 91 não existe
        banco.exigirRestricao("ck_telemetry_latitude", () -> inserirLeitura(91.0, -34.90));
    }

    @Test
    void deveRejeitarLongitudeForaDaFaixa() {
        // Longitude -181 não existe
        banco.exigirRestricao("ck_telemetry_longitude", () -> inserirLeitura(-8.05, -181.0));
    }
}
