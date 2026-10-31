package com.hemovia.api.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/*
Aplica as regras de arquitetura ao código de produção real do projeto.
As classes legadas (controller, service, model...) não casam com estes pacotes,
então as regras passam a valer de verdade conforme cada seção migra o seu pedaço.
*/
// Olhamos só o código de produção; as classes de teste ficam de fora da análise
@AnalyzeClasses(packages = ArquiteturaTest.RAIZ, importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    // Pacote raiz do projeto, compartilhado com a anotação acima
    static final String RAIZ = "com.hemovia.api";

    // Ciclo 1: domínio sem Spring, JPA, Jackson nem camadas externas
    @ArchTest
    static final ArchRule dominioDeveSerJavaPuro = RegrasDeArquitetura.dominioPuro(RAIZ);

    // Ciclo 2a: aplicação sem infraestrutura nem apresentação
    @ArchTest
    static final ArchRule aplicacaoNaoDeveDependerDeInfraestruturaNemApresentacao =
            RegrasDeArquitetura.aplicacaoSemInfraestruturaNemApresentacao(RAIZ);

    // Ciclo 2b: aplicação só com @Service e @Transactional do Spring
    @ArchTest
    static final ArchRule aplicacaoSoDeveUsarServiceETransactionalDoSpring =
            RegrasDeArquitetura.aplicacaoSoUsaServiceETransactionalDoSpring(RAIZ);

    // Ciclo 3: apresentação sem acesso à persistência
    @ArchTest
    static final ArchRule apresentacaoNaoDeveAcessarPersistencia = RegrasDeArquitetura.apresentacaoSemPersistencia(RAIZ);

    // Ciclo 4: controllers sem entidades no retorno
    @ArchTest
    static final ArchRule controllersNaoDevemDevolverEntidades = RegrasDeArquitetura.controllersNaoDevolvemEntidades(RAIZ);
}
