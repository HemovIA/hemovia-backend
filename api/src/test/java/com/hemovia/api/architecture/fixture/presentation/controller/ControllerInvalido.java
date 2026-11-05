package com.hemovia.api.architecture.fixture.presentation.controller;

import com.hemovia.api.architecture.fixture.domain.model.EntidadeFalsa;
import com.hemovia.api.architecture.fixture.infrastructure.persistence.RepositorioFalso;
import com.hemovia.api.architecture.fixture.infrastructure.persistence.entity.EntidadeJpaFalsa;

import java.util.List;

/*
Exemplo propositalmente errado: o controller fala com a persistência e devolve entidades.
*/
public class ControllerInvalido {

    // Acessar o repositório direto da apresentação quebra a regra de camadas
    private RepositorioFalso repositorio;

    // Devolver lista de entidade de domínio expõe o modelo interno pela API
    public List<EntidadeFalsa> listar() {
        return List.of();
    }

    // Devolver entidade JPA expõe o banco pela API
    public EntidadeJpaFalsa buscar() {
        return null;
    }
}
