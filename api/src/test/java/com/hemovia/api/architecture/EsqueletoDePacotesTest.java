package com.hemovia.api.architecture;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/*
Garante que o esqueleto de pacotes da Seção 0.6 do guia existe.
Pacote vazio não sobe no Git, então cada um precisa de um package-info.java que o descreva.
*/
class EsqueletoDePacotesTest {

    // Raiz do código de produção, relativa ao módulo api/ onde o Maven executa os testes
    private static final Path RAIZ = Path.of("src/main/java/com/hemovia/api");

    @ParameterizedTest
    @ValueSource(strings = {
            "domain/model", "domain/valueobject", "domain/enums", "domain/exception", "domain/service",
            "domain/algorithm/compatibility", "domain/algorithm/priority", "domain/algorithm/inventory",
            "domain/algorithm/graph", "domain/algorithm/sorting", "domain/algorithm/search",
            "domain/algorithm/structure",
            "application/usecase", "application/dto/request", "application/dto/response",
            "application/port/input", "application/port/output",
            "infrastructure/persistence/entity", "infrastructure/persistence/repository",
            "infrastructure/persistence/mapper", "infrastructure/persistence/adapter",
            "infrastructure/security", "infrastructure/telemetry", "infrastructure/scheduling",
            "infrastructure/seed", "infrastructure/configuration",
            "presentation/controller", "presentation/handler", "presentation/mapper"})
    void deveTerPackageInfoEmCadaPacoteDoEsqueleto(String pacote) {
        // Dado um pacote do esqueleto da Seção 0.6, quando procuramos o package-info dele
        Path arquivo = RAIZ.resolve(pacote).resolve("package-info.java");
        // Então ele precisa existir, senão o pacote some do repositório
        assertThat(Files.exists(arquivo)).as("falta %s", arquivo).isTrue();
    }
}
