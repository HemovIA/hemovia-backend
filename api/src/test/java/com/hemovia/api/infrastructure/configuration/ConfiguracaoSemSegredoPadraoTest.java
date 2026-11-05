package com.hemovia.api.infrastructure.configuration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/*
Garante por leitura de fonte que nenhum segredo com valor padrão voltou para o código (achado API-003).
*/
class ConfiguracaoSemSegredoPadraoTest {

    // Trecho do segredo que antes ficava escrito no application.yaml e no JwtService
    private static final String SEGREDO_ANTIGO = "hemovia-rota-vital-jwt-secret";

    @Test
    void deveExigirOJwtSecretNoApplicationYamlSemValorPadrao() throws IOException {
        // Dado o application.yaml de produção
        String yaml = Files.readString(Path.of("src/main/resources/application.yaml"));
        // Então o JWT_SECRET é lido sem ":valor-padrão" depois do nome
        assertThat(yaml).contains("${JWT_SECRET}").doesNotContain("${JWT_SECRET:");
    }

    @Test
    void naoDeveSobrarOSegredoAntigoEmNenhumArquivoDeProducao() throws IOException {
        // Percorremos todos os arquivos de src/main procurando o segredo que existia antes
        try (Stream<Path> arquivos = Files.walk(Path.of("src/main"))) {
            assertThat(arquivos.filter(Files::isRegularFile)
                    .filter(arquivo -> contem(arquivo, SEGREDO_ANTIGO))
                    .map(Path::toString)
                    .toList())
                    // Então nenhum arquivo pode conter o trecho
                    .as("arquivos que ainda carregam o segredo antigo")
                    .isEmpty();
        }
    }

    // Lê os bytes como ISO-8859-1, que nunca falha, e diz se o trecho (só ASCII) aparece; erro de leitura quebra o teste
    private static boolean contem(Path arquivo, String trecho) {
        try {
            return new String(Files.readAllBytes(arquivo), StandardCharsets.ISO_8859_1).contains(trecho);
        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível ler " + arquivo, erro);
        }
    }
}
