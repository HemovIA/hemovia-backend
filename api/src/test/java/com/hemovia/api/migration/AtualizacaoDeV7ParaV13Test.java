package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 3 da Seção 2: atualização de um banco que já tem dados no formato da V7.
Quando a V8 a V13 chegarem em produção, nenhuma linha antiga pode sumir nem mudar de sentido:
usuários continuam sem papel, requisições viram MEDIUM e leituras antigas ficam sem posição.
*/
@Tag("integration")
class AtualizacaoDeV7ParaV13Test {

    // Tabelas que recebem uma linha antes da atualização
    private static final String[] TABELAS = {
        "users", "hospital", "blood_centers", "vehicles", "blood_requests",
        "allocations", "delivery_routes", "transports", "telemetry_readings"
    };

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    // Quantas linhas cada tabela tinha antes da atualização
    private Map<String, Long> antes;

    // Ids das linhas antigas que os testes conferem depois
    private UUID usuario;
    private UUID transporte;
    private UUID leitura;

    @BeforeEach
    void prepararBancoComDadosDaV7() throws SQLException {
        // Dado um banco na V7 com uma linha de cada tabela, todas no formato antigo
        banco = new BancoDeMigracao();
        banco.migrarAte("7");
        usuario = banco.criarUsuarioAntigo();
        transporte = banco.criarTransporte();
        leitura = banco.criarLeituraAntiga(transporte);
        antes = contarLinhas();
        // Quando aplicamos o restante das migrations (V8 a V13)
        banco.migrarTudo();
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    // Conta as linhas de cada tabela observada
    private Map<String, Long> contarLinhas() throws SQLException {
        Map<String, Long> contagem = new LinkedHashMap<>();
        for (String tabela : TABELAS) {
            contagem.put(tabela, banco.contar("SELECT count(*) FROM " + tabela));
        }
        return contagem;
    }

    @Test
    void deveChegarNaVersao13() {
        // Então o Flyway termina na última migration
        assertThat(banco.versaoAtual()).isEqualTo("13");
    }

    @Test
    void deveManterTodasAsLinhasAntigas() throws SQLException {
        // O ponto de partida precisa ser real: toda tabela observada tinha pelo menos uma linha
        assertThat(antes.values()).allMatch(total -> total >= 1);
        // Então nenhuma linha sumiu na atualização
        assertThat(contarLinhas()).isEqualTo(antes);
    }

    @Test
    void deveManterUsuarioAntigoSemPapelEAtivo() throws SQLException {
        // O usuário antigo continua sem papel (recebe 403 até um ADMIN atribuir um) e continua ativo
        assertThat(banco.valor("SELECT role FROM users WHERE id = ?", usuario)).isNull();
        assertThat(banco.valor("SELECT active FROM users WHERE id = ?", usuario)).isEqualTo(true);
    }

    @Test
    void deveMarcarRequisicaoAntigaComoMediumSemPrazo() throws SQLException {
        // A requisição antiga ganha a prioridade padrão MEDIUM e continua sem prazo
        assertThat(banco.contar("SELECT count(*) FROM blood_requests WHERE priority = 'MEDIUM' AND delivery_deadline IS NULL")).isEqualTo(1);
    }

    @Test
    void deveManterLeituraAntigaSemPosicao() throws SQLException {
        // A leitura antiga continua com a mesma temperatura e sem latitude nem longitude
        assertThat(banco.contar("SELECT count(*) FROM telemetry_readings WHERE id = ? AND temperature_celsius = 4.5 "
                + "AND latitude IS NULL AND longitude IS NULL", leitura)).isEqualTo(1);
    }

    @Test
    void deveManterVeiculoAntigoSemChaveDeDispositivo() throws SQLException {
        // O veículo do transporte antigo continua sem chave de dispositivo
        assertThat(banco.contar("SELECT count(*) FROM vehicles WHERE device_key_hash IS NULL")).isEqualTo(1);
    }

    @Test
    void deveManterOTransporteAntigoLigadoAosSeusDados() throws SQLException {
        // O transporte antigo continua apontando para a mesma rota, alocação e veículo
        assertThat(banco.contar("SELECT count(*) FROM transports t JOIN allocations a ON a.id = t.allocation_id "
                + "JOIN delivery_routes r ON r.id = t.route_id JOIN vehicles v ON v.id = t.vehicle_id WHERE t.id = ?", transporte)).isEqualTo(1);
    }
}
