package com.hemovia.api.architecture;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

final class RegrasDeArquitetura {

    private RegrasDeArquitetura() { }

    static ArchRule codigoNasCincoCamadas(String raiz) {
        return classes().that().resideInAPackage(raiz + "..")
                .should().resideInAnyPackage(raiz,
                        raiz + ".controller..", raiz + ".service..", raiz + ".dto..",
                        raiz + ".repository..", raiz + ".model..");
    }

    static ArchRule controllersSemPersistencia(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".controller..")
                .should().dependOnClassesThat(resideInAnyPackage(
                        raiz + ".repository..", raiz + ".model.entity..", "org.springframework.data..", "jakarta.persistence..")
                        // O handler global ainda traduz a exceção dos serviços legados para HTTP 404.
                        .and(not(equivalentTo(EntityNotFoundException.class))));
    }

    static ArchRule servicosSemControllers(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".service..")
                .should().dependOnClassesThat().resideInAPackage(raiz + ".controller..");
    }

    static ArchRule repositoriosSemDependenciasDasCamadasSuperiores(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".repository..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        raiz + ".controller..", raiz + ".service..");
    }

    static ArchRule dtosSemPersistenciaNemDependenciasDasCamadasSuperiores(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".dto..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        raiz + ".controller..", raiz + ".service..", raiz + ".repository..",
                        raiz + ".model.entity..", "jakarta.persistence..", "org.springframework.data..");
    }

    static ArchRule modeloSemDependenciasDasCamadasSuperiores(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".model..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        raiz + ".controller..", raiz + ".service..", raiz + ".dto..",
                        raiz + ".repository..", "org.springframework..");
    }

    static ArchRule controllersNaoDevolvemEntidades(String raiz) {
        return methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should(new ArchCondition<JavaMethod>("devolver dados HTTP sem expor o modelo interno") {
                    @Override
                    public void check(JavaMethod metodo, ConditionEvents eventos) {
                        metodo.getReturnType().getAllInvolvedRawTypes().stream()
                                .filter(tipo -> tipo.getPackageName().startsWith(raiz + ".model") && !tipo.isEnum())
                                .forEach(tipo -> eventos.add(SimpleConditionEvent.violated(
                                        metodo, metodo.getFullName() + " devolve " + tipo.getName())));
                    }
                });
    }
}
