package com.hemovia.api.architecture.fixture.application;

import com.hemovia.api.architecture.fixture.infrastructure.persistence.RepositorioFalso;
import org.springframework.stereotype.Component;

/*
Exemplo propositalmente errado: a aplicação conhece a infraestrutura e usa um @Component.
*/
// @Component não está entre as anotações do Spring permitidas na aplicação
@Component
public class AplicacaoInvalida {

    // Depender do repositório concreto da infraestrutura quebra a regra de dependência
    private RepositorioFalso repositorio;
}
