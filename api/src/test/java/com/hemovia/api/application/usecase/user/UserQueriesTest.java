package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.UpdateUserRequest;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.DuplicateResourceException;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import com.hemovia.api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Consultas e edição de usuários: listar, buscar, atualizar e ver quem sou eu.
*/
class UserQueriesTest {

    private CenarioDeAutenticacao cenario;
    private User alice;
    private User bruno;

    @BeforeEach
    void prepararCenario() {
        // Dados dois usuários cadastrados
        cenario = new CenarioDeAutenticacao();
        alice = cenario.salvarAdmin();
        bruno = cenario.salvarUsuario("bruno@hemovia.com", "111.444.777-35", Role.HOSPITAL, cenario.recursos.novoHospital());
    }

    @Test
    void deveListarTodosOsUsuarios() {
        assertThat(cenario.listarUsuarios().execute()).extracting(UserResponse::id).containsExactly(alice.id(), bruno.id());
    }

    @Test
    void deveBuscarUsuarioPeloId() {
        assertThat(cenario.buscarUsuario().execute(bruno.id()).email()).isEqualTo("bruno@hemovia.com");
    }

    @Test
    void deveFalharAoBuscarUsuarioInexistente() {
        UUID fantasma = UUID.randomUUID();
        assertThatThrownBy(() -> cenario.buscarUsuario().execute(fantasma))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Usuário não encontrado: " + fantasma);
    }

    @Test
    void deveAtualizarNomeEmailETelefone() {
        // Quando o ADMIN corrige os dados do Bruno
        UserResponse resposta = cenario.atualizarUsuario().execute(bruno.id(),
                new UpdateUserRequest("Bruno Souza", "BRUNO.souza@hemovia.com", "81977776666"));
        // Então os dados novos ficam, com o e-mail normalizado
        assertThat(resposta.name()).isEqualTo("Bruno Souza");
        assertThat(resposta.email()).isEqualTo("bruno.souza@hemovia.com");
        assertThat(resposta.phoneNumber()).isEqualTo("81977776666");
    }

    @Test
    void devePermitirManterOProprioEmailAoAtualizar() {
        // Salvar sem mudar o e-mail não é duplicidade
        UserResponse resposta = cenario.atualizarUsuario().execute(bruno.id(),
                new UpdateUserRequest("Bruno Souza", "bruno@hemovia.com", null));
        assertThat(resposta.email()).isEqualTo("bruno@hemovia.com");
    }

    @Test
    void deveRecusarEmailQueJaEhDeOutroUsuario() {
        assertThatThrownBy(() -> cenario.atualizarUsuario().execute(bruno.id(),
                new UpdateUserRequest("Bruno", "alice@hemovia.com", null)))
                .isInstanceOf(DuplicateResourceException.class).hasMessage("E-mail já cadastrado");
    }

    @Test
    void deveDevolverOUsuarioAutenticado() {
        // Dado que a Alice é quem está logada
        cenario.usuarioLogado.definir(new AuthenticatedUser(alice.id(), Role.ADMIN, null, null, null));
        // Então "quem sou eu" devolve os dados dela
        assertThat(cenario.usuarioAtual().execute().id()).isEqualTo(alice.id());
    }

    @Test
    void deveFalharQuandoOUsuarioDoTokenNaoExisteMais() {
        // Dado um token de um usuário que já foi removido
        cenario.usuarioLogado.definir(new AuthenticatedUser(UUID.randomUUID(), Role.ADMIN, null, null, null));
        assertThatThrownBy(() -> cenario.usuarioAtual().execute()).isInstanceOf(InvalidCredentialsException.class);
    }
}
