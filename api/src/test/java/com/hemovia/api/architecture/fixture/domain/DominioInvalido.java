package com.hemovia.api.architecture.fixture.domain;

import org.springframework.stereotype.Service;

/*
Exemplo propositalmente errado: um domínio não pode usar anotação do Spring.
*/
// A anotação do Spring aqui é o pecado que a regra do domínio precisa flagrar
@Service
public class DominioInvalido {
}
