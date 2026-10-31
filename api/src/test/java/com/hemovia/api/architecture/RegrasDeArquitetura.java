package com.hemovia.api.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/*
Catálogo das regras de dependência da Clean Architecture (guia, Seção 1).
Cada regra recebe o pacote raiz, assim a mesma régua vale para o código real
e para as classes de exemplo usadas para provar que a régua reprova de verdade.
*/
final class RegrasDeArquitetura {

    // Classe só de métodos estáticos: ninguém precisa instanciar
    private RegrasDeArquitetura() { }

    // Ciclo 1: o domínio é Java puro e não enxerga framework nem as outras camadas
    static ArchRule dominioPuro(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "com.fasterxml..",
                        raiz + ".application..", raiz + ".infrastructure..", raiz + ".presentation..")
                // Os pacotes novos começam vazios, então uma regra sem classes não pode ser erro
                .allowEmptyShould(true);
    }

    // Ciclo 2a: a aplicação não conhece a infraestrutura nem a apresentação
    static ArchRule aplicacaoSemInfraestruturaNemApresentacao(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage(raiz + ".infrastructure..", raiz + ".presentation..")
                .allowEmptyShould(true);
    }

    // Ciclo 2b: do Spring, a aplicação só pode usar @Service e @Transactional
    static ArchRule aplicacaoSoUsaServiceETransactionalDoSpring(String raiz) {
        // Estas são as duas únicas classes do Spring que a camada de aplicação pode citar
        DescribedPredicate<JavaClass> permitidas = equivalentTo(Service.class).or(equivalentTo(Transactional.class));
        return noClasses().that().resideInAPackage(raiz + ".application..")
                .should().dependOnClassesThat(resideInAPackage("org.springframework..").and(DescribedPredicate.not(permitidas)))
                .allowEmptyShould(true);
    }

    // Ciclo 3: a apresentação nunca fala direto com a persistência
    static ArchRule apresentacaoSemPersistencia(String raiz) {
        return noClasses().that().resideInAPackage(raiz + ".presentation..")
                .should().dependOnClassesThat().resideInAPackage(raiz + ".infrastructure.persistence..")
                .allowEmptyShould(true);
    }

    // Ciclo 4: controller devolve DTO, nunca entidade de domínio nem entidade JPA
    static ArchRule controllersNaoDevolvemEntidades(String raiz) {
        return methods().that().areDeclaredInClassesThat().resideInAPackage(raiz + ".presentation.controller..")
                .should(naoDevolverEntidades(raiz))
                .allowEmptyShould(true);
    }

    // Condição própria: olha o tipo de retorno e também os tipos genéricos, como List de entidade
    private static ArchCondition<JavaMethod> naoDevolverEntidades(String raiz) {
        return new ArchCondition<JavaMethod>("não devolver tipos de domain.model nem de infrastructure.persistence.entity") {
            @Override
            public void check(JavaMethod metodo, ConditionEvents eventos) {
                // Cada tipo envolvido no retorno que seja entidade vira uma violação com o nome do método
                metodo.getReturnType().getAllInvolvedRawTypes().stream()
                        .filter(tipo -> tipo.getPackageName().startsWith(raiz + ".domain.model")
                                || tipo.getPackageName().startsWith(raiz + ".infrastructure.persistence.entity"))
                        .forEach(tipo -> eventos.add(SimpleConditionEvent.violated(
                                metodo, metodo.getFullName() + " devolve a entidade " + tipo.getName())));
            }
        };
    }
}
