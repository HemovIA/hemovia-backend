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
Ciclo 2 da Seção 2, migration V10: chave de dispositivo do veículo.
O dispositivo de telemetria se identifica por uma chave; no banco só o hash dela existe, e é único.
Roda contra um PostgreSQL real, num schema isolado, só até a V10.
*/
@Tag("integration")
class MigracaoV10DispositivoTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com as migrations aplicadas até a V10
        banco = new BancoDeMigracao();
        banco.migrarAte("10");
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    // Insere um veículo com o hash informado (nulo significa sem dispositivo) e devolve o id
    private UUID inserirVeiculo(String hashDaChave) throws SQLException {
        UUID id = UUID.randomUUID();
        banco.executar("INSERT INTO vehicles (id, code, capacity, device_key_hash) VALUES (?, ?, 4, ?)", id, "VEI-" + id, hashDaChave);
        return id;
    }

    @Test
    void deveAceitarVariosVeiculosSemChaveDeDispositivo() {
        // Veículos sem dispositivo cadastrado ficam com NULL, e NULL não conta como repetido
        assertThatCode(() -> {
            inserirVeiculo(null);
            inserirVeiculo(null);
        }).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarChavesDiferentesEmVeiculosDiferentes() {
        // Cada veículo com a sua própria chave é o caso normal
        assertThatCode(() -> {
            inserirVeiculo("a".repeat(64));
            inserirVeiculo("b".repeat(64));
        }).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarMesmaChaveEmDoisVeiculos() throws SQLException {
        // Dado um veículo que já usa uma chave
        inserirVeiculo("c".repeat(64));
        // Então um segundo veículo com a mesma chave é recusado, para uma leitura nunca ser atribuída ao veículo errado
        banco.exigirRestricao("uk_vehicles_device_key_hash", () -> inserirVeiculo("c".repeat(64)));
    }

    @Test
    void deveGuardarOHashInformado() throws SQLException {
        // Quando gravamos um veículo com hash
        UUID id = inserirVeiculo("d".repeat(64));
        // Então ele pode ser lido de volta
        assertThat(banco.valor("SELECT device_key_hash FROM vehicles WHERE id = ?", id)).isEqualTo("d".repeat(64));
    }
}
