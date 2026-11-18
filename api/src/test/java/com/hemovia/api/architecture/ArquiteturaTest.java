package com.hemovia.api.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = ArquiteturaTest.RAIZ, importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    static final String RAIZ = "com.hemovia.api";

    @ArchTest
    static final ArchRule codigoNasCincoCamadas = RegrasDeArquitetura.codigoNasCincoCamadas(RAIZ);

    @ArchTest
    static final ArchRule rotasNaCamadaController = classes().that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage(RAIZ + ".controller..");

    @ArchTest
    static final ArchRule casosDeUsoNaCamadaService = classes().that().areAnnotatedWith(Service.class)
            .should().resideInAPackage(RAIZ + ".service.usecase..");

    @ArchTest
    static final ArchRule repositoriosJpaNaCamadaRepository = classes().that().areAssignableTo(JpaRepository.class)
            .should().resideInAPackage(RAIZ + ".repository.jpa..");

    @ArchTest
    static final ArchRule entidadesJpaNaCamadaModel = classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage(RAIZ + ".model.entity..");

    @ArchTest
    static final ArchRule dtosSaoRecords = classes().that().resideInAPackage(RAIZ + ".dto..")
            .and().doNotHaveSimpleName("package-info")
            .should().beAssignableTo(Record.class);

    @ArchTest
    static final ArchRule persistenciaNaoUsaDtosHttp = noClasses().that().resideInAPackage(RAIZ + ".repository..")
            .should().dependOnClassesThat().resideInAnyPackage(RAIZ + ".dto.request..", RAIZ + ".dto.response..");

    @ArchTest
    static final ArchRule controllersSemPersistencia = RegrasDeArquitetura.controllersSemPersistencia(RAIZ);

    @ArchTest
    static final ArchRule servicosSemControllers = RegrasDeArquitetura.servicosSemControllers(RAIZ);

    @ArchTest
    static final ArchRule repositoriosSemDependenciasDasCamadasSuperiores =
            RegrasDeArquitetura.repositoriosSemDependenciasDasCamadasSuperiores(RAIZ);

    @ArchTest
    static final ArchRule dtosSemPersistenciaNemDependenciasDasCamadasSuperiores =
            RegrasDeArquitetura.dtosSemPersistenciaNemDependenciasDasCamadasSuperiores(RAIZ);

    @ArchTest
    static final ArchRule modeloSemDependenciasDasCamadasSuperiores =
            RegrasDeArquitetura.modeloSemDependenciasDasCamadasSuperiores(RAIZ);

    @ArchTest
    static final ArchRule controllersNaoDevemDevolverEntidades =
            RegrasDeArquitetura.controllersNaoDevolvemEntidades(RAIZ);
}
