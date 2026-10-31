package com.hemovia.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Prova que cada regra reprova de verdade: uma regra que nunca falha não protege nada.
Usamos classes de exemplo, de propósito erradas, em architecture.fixture.
*/
class RegrasDeArquiteturaTest {

    // Pacote raiz das classes de exemplo, que imitam a estrutura domain/application/presentation
    private static final String RAIZ = "com.hemovia.api.architecture.fixture";

    // Importamos as classes de exemplo uma única vez, porque a leitura dos .class é cara
    private static final JavaClasses EXEMPLOS = new ClassFileImporter().importPackages(RAIZ);

    // Aplica a regra nas classes de exemplo e confirma que ela reprova citando o culpado
    private static void deveReprovarCitando(ArchRule regra, String culpado) {
        // A verificação lança AssertionError quando alguma classe quebra a regra
        assertThatThrownBy(() -> regra.check(EXEMPLOS))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(culpado);
    }

    @Test
    void deveReprovarDominioQueUsaSpring() {
        // Dado um domínio anotado com @Service, quando a régua roda, então ela aponta essa classe
        deveReprovarCitando(RegrasDeArquitetura.dominioPuro(RAIZ), "DominioInvalido");
    }

    @Test
    void deveReprovarAplicacaoQueUsaInfraestrutura() {
        // Dada uma aplicação com campo de repositório da infraestrutura
        deveReprovarCitando(RegrasDeArquitetura.aplicacaoSemInfraestruturaNemApresentacao(RAIZ), "AplicacaoInvalida");
    }

    @Test
    void deveReprovarAplicacaoQueUsaSpringAlemDeServiceETransactional() {
        // Dada uma aplicação anotada com @Component, que não está na lista permitida
        deveReprovarCitando(RegrasDeArquitetura.aplicacaoSoUsaServiceETransactionalDoSpring(RAIZ), "AplicacaoInvalida");
    }

    @Test
    void deveReprovarApresentacaoQueAcessaPersistencia() {
        // Dado um controller com campo do repositório de persistência
        deveReprovarCitando(RegrasDeArquitetura.apresentacaoSemPersistencia(RAIZ), "ControllerInvalido");
    }

    @Test
    void deveReprovarControllerQueDevolveEntidade() {
        // Dado um controller que devolve lista de entidade de domínio e entidade JPA
        deveReprovarCitando(RegrasDeArquitetura.controllersNaoDevolvemEntidades(RAIZ), "ControllerInvalido.listar");
    }
}
