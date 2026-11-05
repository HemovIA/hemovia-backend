package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 2 da Seção 2, migration V11: prioridade e prazo de entrega da requisição.
As requisições antigas viram MEDIUM, e o banco só aceita as quatro prioridades do sistema.
Roda contra um PostgreSQL real, num schema isolado, só até a V11.
*/
@Tag("integration")
class MigracaoV11RequisicaoTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    // Requisição criada no formato antigo, sem prioridade nem prazo
    private UUID requisicao;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com as migrations aplicadas até a V11 e uma requisição criada sem informar prioridade
        banco = new BancoDeMigracao();
        banco.migrarAte("11");
        requisicao = banco.criarRequisicao(banco.criarHospital());
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    @Test
    void deveUsarMediumQuandoAPrioridadeNaoEhInformada() throws SQLException {
        // Então a requisição nasce com prioridade MEDIUM
        assertThat(banco.valor("SELECT priority FROM blood_requests WHERE id = ?", requisicao)).isEqualTo("MEDIUM");
    }

    @Test
    void devePermitirRequisicaoSemPrazoNoBanco() throws SQLException {
        // O prazo é opcional no banco por causa das linhas antigas (o DTO é que exige)
        assertThat(banco.valor("SELECT delivery_deadline FROM blood_requests WHERE id = ?", requisicao)).isNull();
    }

    @Test
    void deveAceitarAsQuatroPrioridades() throws SQLException {
        // Cada uma das quatro prioridades do sistema é aceita
        for (String prioridade : new String[] {"LOW", "MEDIUM", "HIGH", "CRITICAL"}) {
            banco.executar("UPDATE blood_requests SET priority = ? WHERE id = ?", prioridade, requisicao);
            assertThat(banco.valor("SELECT priority FROM blood_requests WHERE id = ?", requisicao)).isEqualTo(prioridade);
        }
    }

    @Test
    void deveRejeitarPrioridadeUrgent() {
        // URGENT não existe no sistema, e o banco recusa
        banco.exigirRestricao("ck_blood_requests_priority",
                () -> banco.executar("UPDATE blood_requests SET priority = 'URGENT' WHERE id = ?", requisicao));
    }

    @Test
    void deveRejeitarPrioridadeNula() {
        // A prioridade é obrigatória: nula não passa
        banco.exigirRejeicao("null value in column \"priority\"",
                () -> banco.executar("UPDATE blood_requests SET priority = NULL WHERE id = ?", requisicao));
    }

    @Test
    void deveGuardarOPrazoDeEntrega() throws SQLException {
        // Quando informamos um prazo
        banco.executar("UPDATE blood_requests SET delivery_deadline = TIMESTAMPTZ '2026-08-12 12:00:00+00' WHERE id = ?", requisicao);
        // Então ele fica gravado
        assertThat(banco.valor("SELECT delivery_deadline FROM blood_requests WHERE id = ?", requisicao)).isNotNull();
    }
}
