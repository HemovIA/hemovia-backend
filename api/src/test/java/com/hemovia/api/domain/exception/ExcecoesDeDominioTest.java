package com.hemovia.api.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Confere as exceções de negócio da Seção 1: o código estável e a mensagem em português.
O domínio não sabe de HTTP, então aqui só existe o código; quem decide o status é o handler.
*/
class ExcecoesDeDominioTest {

    @Test
    void deveMontarMensagemDeRecursoNaoEncontradoComONomeEOId() {
        // Dado um id qualquer
        UUID id = UUID.randomUUID();
        // Quando criamos a exceção de recurso inexistente
        ResourceNotFoundException excecao = new ResourceNotFoundException("Hospital", id);
        // Então a mensagem diz qual recurso e qual id, e o código é NOT_FOUND
        assertThat(excecao).hasMessage("Hospital não encontrado: " + id);
        assertThat(excecao.code()).isEqualTo("NOT_FOUND");
    }

    @Test
    void deveExporCodigoDeRecursoDuplicado() {
        // Um recurso duplicado carrega a mensagem recebida e o código DUPLICATE_RESOURCE
        DuplicateResourceException excecao = new DuplicateResourceException("E-mail já cadastrado");
        assertThat(excecao).hasMessage("E-mail já cadastrado");
        assertThat(excecao.code()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    void deveExporCodigoDeCredenciaisInvalidas() {
        // Credencial inválida é tratada como UNAUTHORIZED pela API
        InvalidCredentialsException excecao = new InvalidCredentialsException("Credenciais inválidas");
        assertThat(excecao).hasMessage("Credenciais inválidas");
        assertThat(excecao.code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void deveExporCodigoEMensagemDeBolsaVencida() {
        // A bolsa vencida tem mensagem fixa em português e o código BLOOD_BAG_EXPIRED
        BloodBagExpiredException excecao = new BloodBagExpiredException();
        assertThat(excecao).hasMessage("A data de validade da bolsa deve estar no futuro.");
        assertThat(excecao.code()).isEqualTo("BLOOD_BAG_EXPIRED");
    }

    @Test
    void deveManterATransicaoInvalidaComoIllegalStateException() {
        // POOUS09 pede que a transição inválida seja uma IllegalStateException
        assertThat(new InvalidStateTransitionException("transição proibida")).isInstanceOf(IllegalStateException.class);
    }
}
