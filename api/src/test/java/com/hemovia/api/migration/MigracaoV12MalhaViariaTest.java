package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/*
Ciclo 2 da Seção 2, migration V12: malha viária (nós e arestas) usada pelo Dijkstra.
Um nó pode ser um hemocentro, um hospital ou um cruzamento, e uma aresta liga dois nós diferentes.
Roda contra um PostgreSQL real, num schema isolado, só até a V12.
*/
@Tag("integration")
class MigracaoV12MalhaViariaTest {

    // Banco isolado deste teste
    private BancoDeMigracao banco;

    @BeforeEach
    void prepararBanco() throws SQLException {
        // Dado um banco vazio com as migrations aplicadas até a V12
        banco = new BancoDeMigracao();
        banco.migrarAte("12");
    }

    @AfterEach
    void apagarBanco() throws SQLException {
        // No fim, o schema do teste é apagado
        banco.close();
    }

    // Insere um nó e devolve o id; hemocentro e hospital nulos significam cruzamento
    private UUID criarNo(double latitude, double longitude, UUID hemocentro, UUID hospital) throws SQLException {
        UUID id = UUID.randomUUID();
        banco.executar("INSERT INTO route_nodes (id, name, latitude, longitude, blood_center_id, hospital_id) VALUES (?, 'Nó de teste', ?, ?, ?, ?)",
                id, latitude, longitude, hemocentro, hospital);
        return id;
    }

    // Insere uma aresta e devolve o id
    private UUID criarAresta(UUID de, UUID para, String km, long segundos) throws SQLException {
        UUID id = UUID.randomUUID();
        banco.executar("INSERT INTO route_edges (id, from_node_id, to_node_id, distance_km, travel_seconds) VALUES (?, ?, ?, ?, ?)",
                id, de, para, new BigDecimal(km), segundos);
        return id;
    }

    @Test
    void deveAceitarCruzamentoSemHemocentroNemHospital() {
        // Um cruzamento intermediário não pertence a ninguém
        assertThatCode(() -> criarNo(-8.05, -34.90, null, null)).doesNotThrowAnyException();
    }

    @Test
    void deveAceitarNoDeHemocentroENoDeHospital() throws SQLException {
        // Dados um hemocentro e um hospital
        UUID hemocentro = banco.criarHemocentro();
        UUID hospital = banco.criarHospital();
        // Então cada um pode ter o seu nó
        assertThatCode(() -> {
            criarNo(-8.06, -34.88, hemocentro, null);
            criarNo(-8.05, -34.90, null, hospital);
        }).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarNoQueSeriaHemocentroEHospitalAoMesmoTempo() throws SQLException {
        // Dados um hemocentro e um hospital
        UUID hemocentro = banco.criarHemocentro();
        UUID hospital = banco.criarHospital();
        // Então um mesmo nó não pode representar os dois
        banco.exigirRestricao("ck_route_nodes_single_owner", () -> criarNo(-8.05, -34.90, hemocentro, hospital));
    }

    @Test
    void deveRejeitarDoisNosParaOMesmoHemocentro() throws SQLException {
        // Dado um hemocentro que já tem nó
        UUID hemocentro = banco.criarHemocentro();
        criarNo(-8.06, -34.88, hemocentro, null);
        // Então um segundo nó para ele é recusado
        banco.exigirRestricao("route_nodes_blood_center_id_key", () -> criarNo(-8.07, -34.89, hemocentro, null));
    }

    @Test
    void deveRejeitarDoisNosParaOMesmoHospital() throws SQLException {
        // Dado um hospital que já tem nó
        UUID hospital = banco.criarHospital();
        criarNo(-8.05, -34.90, null, hospital);
        // Então um segundo nó para ele é recusado
        banco.exigirRestricao("route_nodes_hospital_id_key", () -> criarNo(-8.04, -34.91, null, hospital));
    }

    @Test
    void deveRejeitarLatitudeForaDaFaixa() {
        // Latitude só vai de -90 a 90
        banco.exigirRestricao("ck_route_nodes_latitude", () -> criarNo(91, -34.90, null, null));
    }

    @Test
    void deveRejeitarLongitudeForaDaFaixa() {
        // Longitude só vai de -180 a 180
        banco.exigirRestricao("ck_route_nodes_longitude", () -> criarNo(-8.05, -181, null, null));
    }

    @Test
    void deveRejeitarArestaDeUmNoParaEleMesmo() throws SQLException {
        // Dado um nó qualquer
        UUID no = criarNo(-8.05, -34.90, null, null);
        // Então uma aresta que sai dele e volta para ele mesmo é recusada
        banco.exigirRestricao("ck_route_edges_no_loop", () -> criarAresta(no, no, "1.5", 120));
    }

    @Test
    void deveRejeitarDistanciaZeroOuNegativa() throws SQLException {
        // Dados dois nós
        UUID a = criarNo(-8.05, -34.90, null, null);
        UUID b = criarNo(-8.06, -34.91, null, null);
        // Então distância zero ou negativa é recusada
        banco.exigirRestricao("ck_route_edges_distance", () -> criarAresta(a, b, "0", 120));
        banco.exigirRestricao("ck_route_edges_distance", () -> criarAresta(a, b, "-2.5", 120));
    }

    @Test
    void deveRejeitarTempoDeViagemZeroOuNegativo() throws SQLException {
        // Dados dois nós
        UUID a = criarNo(-8.05, -34.90, null, null);
        UUID b = criarNo(-8.06, -34.91, null, null);
        // Então tempo zero ou negativo é recusado
        banco.exigirRestricao("ck_route_edges_travel", () -> criarAresta(a, b, "1.5", 0));
        banco.exigirRestricao("ck_route_edges_travel", () -> criarAresta(a, b, "1.5", -30));
    }

    @Test
    void deveRejeitarArestaRepetidaNoMesmoSentido() throws SQLException {
        // Dada uma aresta de A para B
        UUID a = criarNo(-8.05, -34.90, null, null);
        UUID b = criarNo(-8.06, -34.91, null, null);
        criarAresta(a, b, "1.5", 120);
        // Então outra de A para B é recusada
        banco.exigirRestricao("uk_route_edges_pair", () -> criarAresta(a, b, "2.0", 150));
    }

    @Test
    void deveRejeitarArestaParaNoQueNaoExiste() throws SQLException {
        // Dado um nó real
        UUID no = criarNo(-8.05, -34.90, null, null);
        // Então uma aresta para um nó que não existe é recusada pela chave estrangeira
        banco.exigirRestricao("route_edges_to_node_id_fkey", () -> criarAresta(no, UUID.randomUUID(), "1.5", 120));
    }

    @Test
    void deveMarcarArestaComoBidirecionalPorPadrao() throws SQLException {
        // Dada uma aresta criada sem informar o sentido
        UUID a = criarNo(-8.05, -34.90, null, null);
        UUID b = criarNo(-8.06, -34.91, null, null);
        UUID aresta = criarAresta(a, b, "1.5", 120);
        // Então ela vale nos dois sentidos
        assertThat(banco.valor("SELECT bidirectional FROM route_edges WHERE id = ?", aresta)).isEqualTo(true);
    }

    @Test
    void deveCriarIndicesDeOrigemEDestinoDasArestas() throws SQLException {
        // O Dijkstra percorre as arestas por nó de saída e de chegada, então os dois têm índice
        banco.exigirIndice("ix_route_edges_from");
        banco.exigirIndice("ix_route_edges_to");
    }
}
