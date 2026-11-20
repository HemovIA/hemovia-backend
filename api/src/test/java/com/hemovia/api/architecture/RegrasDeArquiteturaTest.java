package com.hemovia.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegrasDeArquiteturaTest {

    private static final String RAIZ = "com.hemovia.api.architecture.fixture";
    private static final JavaClasses EXEMPLOS = new ClassFileImporter().importPackages(RAIZ);

    private static void deveReprovarCitando(ArchRule regra, String culpado) {
        assertThatThrownBy(() -> regra.check(EXEMPLOS))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(culpado);
    }

    @Test
    void deveReprovarModeloQueUsaSpring() {
        deveReprovarCitando(RegrasDeArquitetura.modeloSemDependenciasDasCamadasSuperiores(RAIZ), "DominioInvalido");
    }

    @Test
    void deveReprovarServicoQueUsaController() {
        deveReprovarCitando(RegrasDeArquitetura.servicosSemControllers(RAIZ), "AplicacaoInvalida");
    }

    @Test
    void deveReprovarControllerQueAcessaPersistencia() {
        deveReprovarCitando(RegrasDeArquitetura.controllersSemPersistencia(RAIZ), "ControllerInvalido");
    }

    @Test
    void deveReprovarControllerQueDevolveModelo() {
        deveReprovarCitando(RegrasDeArquitetura.controllersNaoDevolvemEntidades(RAIZ), "ControllerInvalido.listar");
    }
}
