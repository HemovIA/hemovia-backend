package com.hemovia.api.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 5 da Seção 1: garante a regra 0.1 do guia em todos os arquivos .java.
Os arquivos antigos que ainda têm Javadoc ficam numa lista de legados,
e cada seção do guia remove da lista os que migrou, até ela esvaziar.
*/
class ComentariosTest {

    // Pasta de código-fonte, relativa ao módulo api/ onde o Maven executa os testes
    private static final Path SRC = Path.of("src");

    // Legados com Javadoc ou asterisco; só podem sair desta lista, nunca entrar novos
    private static final Set<String> LEGADOS = Set.of(
            "main/java/com/hemovia/api/domain/BloodCompatibility.java",
            "main/java/com/hemovia/api/model/BloodRequest.java",
            "main/java/com/hemovia/api/model/DTOs/DonationResponseDTO.java",
            "main/java/com/hemovia/api/model/DeliveryRoute.java",
            "main/java/com/hemovia/api/model/Donation.java",
            "main/java/com/hemovia/api/model/RouteWaypoint.java",
            "main/java/com/hemovia/api/model/StockMovement.java",
            "main/java/com/hemovia/api/model/TelemetryReading.java",
            "main/java/com/hemovia/api/model/Transport.java",
            "main/java/com/hemovia/api/model/enums/AllocationStatus.java",
            "main/java/com/hemovia/api/model/enums/MovementType.java",
            "main/java/com/hemovia/api/model/enums/RequestStatus.java",
            "main/java/com/hemovia/api/model/enums/ReservationStatus.java",
            "main/java/com/hemovia/api/model/enums/TransportStatus.java",
            "main/java/com/hemovia/api/structures/basic/BinarySearchTree.java",
            "main/java/com/hemovia/api/structures/basic/BloodBagStockList.java",
            "main/java/com/hemovia/api/structures/basic/StockHistoryStack.java",
            "test/java/com/hemovia/api/acceptance/DonationAcceptanceTest.java",
            "test/java/com/hemovia/api/acceptance/HospitalAcceptanceTest.java",
            "test/java/com/hemovia/api/domain/BloodCompatibilityTest.java",
            "test/java/com/hemovia/api/integration/HospitalRepositoryIntegrationTest.java",
            "test/java/com/hemovia/api/model/BaseEntitiesPersistenceIT.java",
            "test/java/com/hemovia/api/support/BaseEntitiesPostgres.java",
            "test/java/com/hemovia/api/support/BaseEntityFixtures.java");

    @Test
    void deveDetectarJavadoc() {
        // Dado um texto com Javadoc de verdade (montado em pedaços para este arquivo não se denunciar)
        String fonte = "/" + "**\n Texto\n */\nclass A { }";
        // Quando a régua olha o texto, então ela acusa o Javadoc na linha 1
        assertThat(RegraDeComentarios.violacoes(fonte)).contains("linha 1: Javadoc proibido");
    }

    @Test
    void deveDetectarLinhaIniciadaPorAsterisco() {
        // Dado um bloco no estilo antigo, com asterisco no começo da linha do meio
        String fonte = "/*\n * Texto\n*/\nclass A { }";
        // Quando a régua olha o texto, então ela acusa a linha 2
        assertThat(RegraDeComentarios.violacoes(fonte)).containsExactly("linha 2: linha iniciada por asterisco");
    }

    @Test
    void deveAceitarComentarioDeLinhaEBlocoSimples() {
        // Dado um bloco que abre com barra e asterisco, tem texto normal e fecha com asterisco e barra
        String fonte = "/*\nTexto simples\nem duas linhas\n*/\n// comentário de linha\nclass A { }";
        // Quando a régua olha o texto, então não há nada a reclamar
        assertThat(RegraDeComentarios.violacoes(fonte)).isEmpty();
    }

    @Test
    void deveAceitarBarraEDoisAsteriscosDentroDeTexto() {
        // Dado um padrão de rota que termina em barra e dois asteriscos dentro de aspas
        String fonte = "String rota = \"/actuator/" + "**\";";
        // Quando a régua olha o texto, então rota não é Javadoc
        assertThat(RegraDeComentarios.violacoes(fonte)).isEmpty();
    }

    @Test
    void deveManterNaListaDeLegadosSomenteArquivosQueAindaViolam() throws IOException {
        // Para cada arquivo listado como legado, lemos o conteúdo atual
        for (String legado : LEGADOS) {
            // Um legado que já está limpo deve sair da lista, para ninguém esconder arquivo novo ali
            assertThat(RegraDeComentarios.violacoes(Files.readString(SRC.resolve(legado))))
                    .as("o arquivo %s já está limpo e deve sair da lista de legados", legado)
                    .isNotEmpty();
        }
    }

    @Test
    void nenhumArquivoForaDoLegadoDeveViolarARegraDeComentarios() {
        // Quando varremos todo o src, ignorando só os legados declarados
        assertThat(RegraDeComentarios.violacoesDoProjeto(SRC, LEGADOS))
                // Então não pode sobrar nenhum Javadoc nem linha iniciada por asterisco
                .as("arquivos fora da lista de legados com Javadoc ou asterisco")
                .isEmpty();
    }
}
