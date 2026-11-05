package com.hemovia.api.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 1 da Seção 2: as migrations já publicadas (V1 a V7) são imutáveis.
Uma migration alterada depois de aplicada quebra o Flyway em produção, então guardamos o checksum de cada uma.
O banco só cresce com arquivos novos, de V8 a V13, sem buracos na numeração.
*/
class MigrationSourcesTest {

    // Pasta das migrations, relativa ao módulo api/ onde o Maven executa os testes
    private static final Path PASTA = Path.of("src/main/resources/db/migration");

    // Nome de arquivo de migration versionada: V<número>__<descrição>.sql
    private static final Pattern NOME = Pattern.compile("V(\\d+)__.+\\.sql");

    @Test
    void deveManterIguaisAsMigrationsDeV1AV7() throws Exception {
        // Dado o manifesto com o checksum que cada migration antiga tinha quando foi publicada
        Map<String, String> esperados = lerManifesto();
        // Então ele cobre exatamente as sete migrations antigas
        assertThat(esperados).hasSize(7);
        for (Map.Entry<String, String> entrada : esperados.entrySet()) {
            // Quando calculamos o checksum do arquivo que está no código hoje
            String atual = checksumDe(entrada.getKey());
            // Então ele é idêntico ao do manifesto
            assertThat(atual).as("Não reescreva migration já publicada: " + entrada.getKey()).isEqualTo(entrada.getValue());
        }
    }

    @Test
    void deveTerMigrationsSequenciaisDeV1AV13() throws IOException {
        // Quando listamos as versões que existem na pasta de migrations
        List<Integer> versoes = versoesExistentes();
        // Então vão de 1 a 13, sem repetir número e sem pular nenhum
        assertThat(versoes).containsExactlyElementsOf(IntStream.rangeClosed(1, 13).boxed().toList());
    }

    // Lê o manifesto de checksums que fica junto dos recursos de teste
    private Map<String, String> lerManifesto() throws IOException {
        try (var manifesto = getClass().getClassLoader().getResourceAsStream("fixtures/base-entities-migration-checksums.json")) {
            assertThat(manifesto).as("manifesto de checksums").isNotNull();
            return new ObjectMapper().readValue(manifesto, new TypeReference<>() { });
        }
    }

    // Calcula o SHA-256 do arquivo com quebras de linha normalizadas, para o Windows (CRLF) e o Linux (LF) darem o mesmo valor
    private String checksumDe(String arquivo) throws Exception {
        try (var recurso = getClass().getClassLoader().getResourceAsStream("db/migration/" + arquivo)) {
            assertThat(recurso).as("migration " + arquivo).isNotNull();
            String texto = new String(recurso.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        }
    }

    // Extrai o número de versão de cada arquivo da pasta, já em ordem crescente
    private List<Integer> versoesExistentes() throws IOException {
        try (Stream<Path> arquivos = Files.list(PASTA)) {
            return arquivos
                    .map(caminho -> NOME.matcher(caminho.getFileName().toString()))
                    .filter(Matcher::matches)
                    .map(encontro -> Integer.parseInt(encontro.group(1)))
                    .sorted()
                    .toList();
        }
    }
}
