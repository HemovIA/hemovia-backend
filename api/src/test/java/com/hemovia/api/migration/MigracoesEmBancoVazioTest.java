package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 2 da Seção 2: as treze migrations aplicam, em ordem, num banco totalmente vazio.
É o caminho de quem sobe o sistema pela primeira vez (e o do CI).
*/
@Tag("integration")
class MigracoesEmBancoVazioTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio, sem nenhuma tabela
        banco = new BancoDeMigracao();
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    @Test
    void deveAplicarTodasAsMigrationsEmBancoVazio() {
        // Quando aplicamos todas as migrations
        banco.migrarTudo();
        // Então o banco termina na versão 13
        assertThat(banco.versaoAtual()).isEqualTo("13");
    }

    @Test
    void deveCriarAsTabelasNovasDaSecao2() throws SQLException {
        // Quando aplicamos todas as migrations
        banco.migrarTudo();
        // Então as três tabelas novas existem
        long tabelas = banco.contar("SELECT count(*) FROM information_schema.tables WHERE table_schema = current_schema() "
                + "AND table_name IN ('refresh_tokens', 'route_nodes', 'route_edges')");
        assertThat(tabelas).isEqualTo(3);
    }
}
