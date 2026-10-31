package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.hemovia.api.application.CenarioDeAutenticacao.SENHA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 11 a 13 da Seção 3: o refresh gira o token e detecta reuso de um token já revogado.
*/
class RefreshSessionTest {

    private CenarioDeAutenticacao cenario;
    private User alice;
    private SessionTokens primeiraSessao;

    @BeforeEach
    void prepararCenario() {
        // Dada uma administradora que acabou de entrar
        cenario = new CenarioDeAutenticacao();
        alice = cenario.salvarAdmin();
        primeiraSessao = cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA));
    }

    @Test
    void deveGirarOTokenRevogandoOAntigoELigandoAoNovo() {
        // Quando ela renova a sessão
        SessionTokens nova = cenario.refresh().execute(primeiraSessao.refreshToken());
        // Então recebe um refresh token diferente
        assertThat(nova.refreshToken()).isNotEqualTo(primeiraSessao.refreshToken());
        // E o antigo ficou revogado, apontando para o novo, na mesma família
        RefreshToken antigo = cenario.tokens.todos().get(0);
        RefreshToken novo = cenario.tokens.todos().get(1);
        assertThat(antigo.isRevoked()).isTrue();
        assertThat(antigo.replacedBy()).isEqualTo(novo.id());
        assertThat(novo.familyId()).isEqualTo(antigo.familyId());
        assertThat(novo.isRevoked()).isFalse();
    }

    @Test
    void deveEntregarUmAccessTokenNovoDoMesmoUsuario() {
        // A renovação devolve também o usuário e um access token válido
        SessionTokens nova = cenario.refresh().execute(primeiraSessao.refreshToken());
        assertThat(nova.accessToken()).isEqualTo("access-" + alice.id());
        assertThat(nova.user().id()).isEqualTo(alice.id());
    }

    @Test
    void deveRevogarAFamiliaInteiraQuandoUmTokenRevogadoEhReapresentado() {
        // Dado que o token original já foi trocado por outro
        cenario.refresh().execute(primeiraSessao.refreshToken());
        // Quando alguém reapresenta o token original (sinal de roubo)
        assertThatThrownBy(() -> cenario.refresh().execute(primeiraSessao.refreshToken()))
                .isInstanceOf(InvalidCredentialsException.class);
        // Então todos os tokens da família ficam revogados, inclusive o mais novo
        assertThat(cenario.tokens.todos()).allMatch(RefreshToken::isRevoked);
    }

    @Test
    void naoDeveMexerEmOutraFamiliaAoRevogarUmaFamilia() {
        // Dada uma segunda sessão da mesma pessoa, em outro aparelho
        SessionTokens outraSessao = cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA));
        // Quando a primeira sessão sofre reuso de token
        cenario.refresh().execute(primeiraSessao.refreshToken());
        assertThatThrownBy(() -> cenario.refresh().execute(primeiraSessao.refreshToken()))
                .isInstanceOf(InvalidCredentialsException.class);
        // Então a outra sessão continua funcionando
        assertThat(cenario.refresh().execute(outraSessao.refreshToken()).user().id()).isEqualTo(alice.id());
    }

    @Test
    void deveRecusarTokenVencido() {
        // Passados 8 dias, o refresh token de 7 dias venceu
        cenario.relogio.avancar(Duration.ofDays(8));
        assertThatThrownBy(() -> cenario.refresh().execute(primeiraSessao.refreshToken()))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveRecusarTokenDesconhecidoVazioOuNulo() {
        // Valores que nunca foram emitidos, em branco ou ausentes, dão a mesma falha genérica
        for (String invalido : new String[] {"nunca-emitido", "  ", null}) {
            assertThatThrownBy(() -> cenario.refresh().execute(invalido)).isInstanceOf(InvalidCredentialsException.class);
        }
    }

    @Test
    void deveRecusarRenovacaoDeUsuarioDesativadoDepoisDoLogin() {
        // Dada uma conta desativada depois do login
        alice.deactivate();
        // Então o refresh não renova mais a sessão
        assertThatThrownBy(() -> cenario.refresh().execute(primeiraSessao.refreshToken()))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
