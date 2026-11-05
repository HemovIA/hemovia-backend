package com.hemovia.api.application.usecase.user;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.ChangeUserRoleRequest;
import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.application.dto.response.UserResponse;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.hemovia.api.application.CenarioDeAutenticacao.SENHA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 18 e 19 da Seção 3: trocar o papel muda o vínculo junto, e desativar o usuário derruba as sessões dele.
*/
class ChangeAndDeactivateUserTest {

    private CenarioDeAutenticacao cenario;
    private User alice;

    @BeforeEach
    void prepararCenario() {
        // Dada uma administradora cadastrada
        cenario = new CenarioDeAutenticacao();
        alice = cenario.salvarAdmin();
    }

    @Test
    void deveTrocarOPapelEOVinculoJuntos() {
        // Quando a administradora passa a ser usuária de um hospital
        UUID hospital = cenario.recursos.novoHospital();
        UserResponse resposta = cenario.trocarPapel().execute(alice.id(), new ChangeUserRoleRequest(Role.HOSPITAL, hospital, null, null));
        // Então papel e vínculo mudam e ficam guardados
        assertThat(resposta.role()).isEqualTo(Role.HOSPITAL);
        assertThat(resposta.hospitalId()).isEqualTo(hospital);
        assertThat(cenario.usuarios.findById(alice.id()).orElseThrow().role()).isEqualTo(Role.HOSPITAL);
    }

    @Test
    void deveRecusarVinculoInexistenteNaTrocaDePapel() {
        UUID fantasma = UUID.randomUUID();
        assertThatThrownBy(() -> cenario.trocarPapel().execute(alice.id(), new ChangeUserRoleRequest(Role.HOSPITAL, fantasma, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
        // E o papel antigo continua
        assertThat(alice.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveRecusarTrocaDeUsuarioInexistente() {
        UUID fantasma = UUID.randomUUID();
        assertThatThrownBy(() -> cenario.trocarPapel().execute(fantasma, new ChangeUserRoleRequest(Role.ADMIN, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Usuário não encontrado: " + fantasma);
    }

    @Test
    void deveDesativarERevogarTodosOsRefreshTokensDoUsuario() {
        // Dado um usuário com duas sessões abertas, em dois aparelhos
        cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA));
        SessionTokens segunda = cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA));
        // Quando ele é desativado
        cenario.desativarUsuario().execute(alice.id());
        // Então ele fica inativo e as duas sessões morrem
        assertThat(alice.isActive()).isFalse();
        assertThat(cenario.tokens.todos()).hasSize(2).allMatch(RefreshToken::isRevoked);
        assertThatThrownBy(() -> cenario.refresh().execute(segunda.refreshToken())).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveImpedirOLoginDeUsuarioDesativado() {
        cenario.desativarUsuario().execute(alice.id());
        assertThatThrownBy(() -> cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveRecusarDesativacaoDeUsuarioInexistente() {
        assertThatThrownBy(() -> cenario.desativarUsuario().execute(UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);
    }
}
