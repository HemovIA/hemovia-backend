package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.RefreshToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.hemovia.api.application.CenarioDeAutenticacao.AGORA;
import static com.hemovia.api.application.CenarioDeAutenticacao.SENHA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclo 14 da Seção 3: o logout revoga o refresh token atual e nunca quebra.
*/
class LogoutTest {

    private CenarioDeAutenticacao cenario;
    private SessionTokens sessao;

    @BeforeEach
    void prepararCenario() {
        // Dada uma administradora com a sessão aberta
        cenario = new CenarioDeAutenticacao();
        cenario.salvarAdmin();
        sessao = cenario.login().execute(new LoginRequest("alice@hemovia.com", SENHA));
    }

    @Test
    void deveRevogarORefreshTokenAtual() {
        // Quando ela sai
        cenario.logout().execute(sessao.refreshToken());
        // Então o token fica revogado e não renova mais a sessão
        assertThat(cenario.tokens.todos().get(0).isRevoked()).isTrue();
        assertThatThrownBy(() -> cenario.refresh().execute(sessao.refreshToken())).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveSerIdempotenteComTokenDesconhecidoVazioOuNulo() {
        // Sair com um token que não existe, em branco ou ausente não dá erro
        for (String invalido : new String[] {"nunca-emitido", "", null}) {
            assertThatCode(() -> cenario.logout().execute(invalido)).doesNotThrowAnyException();
        }
    }

    @Test
    void deveManterOMomentoDoPrimeiroLogoutQuandoSaiDeNovo() {
        // Dado um logout feito agora
        cenario.logout().execute(sessao.refreshToken());
        // Quando ela repete o logout uma hora depois
        cenario.relogio.avancar(Duration.ofHours(1));
        cenario.logout().execute(sessao.refreshToken());
        // Então o registro da primeira revogação não muda
        RefreshToken token = cenario.tokens.todos().get(0);
        assertThat(token.revokedAt()).isEqualTo(AGORA);
    }
}
