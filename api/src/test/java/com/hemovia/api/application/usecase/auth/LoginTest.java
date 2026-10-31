package com.hemovia.api.application.usecase.auth;

import com.hemovia.api.application.CenarioDeAutenticacao;
import com.hemovia.api.application.dto.request.LoginRequest;
import com.hemovia.api.application.dto.response.SessionTokens;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static com.hemovia.api.application.CenarioDeAutenticacao.AGORA;
import static com.hemovia.api.application.CenarioDeAutenticacao.SENHA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclos 7 a 10 da Seção 3: login por e-mail ou CPF, sempre com a mesma mensagem genérica quando falha.
Os testes de e-mail, CPF, senha errada e usuário inexistente caracterizam o que o AuthServiceImpl antigo já fazia.
*/
class LoginTest {

    private CenarioDeAutenticacao cenario;
    private User alice;

    @BeforeEach
    void prepararCenario() {
        // Dada uma administradora ativa cadastrada
        cenario = new CenarioDeAutenticacao();
        alice = cenario.salvarAdmin();
    }

    private SessionTokens entrar(String identificador, String senha) {
        return cenario.login().execute(new LoginRequest(identificador, senha));
    }

    @Test
    void deveEntrarComEmail() {
        // Quando ela entra com o e-mail e a senha certa
        SessionTokens sessao = entrar("alice@hemovia.com", SENHA);
        // Então recebe os dois tokens e os próprios dados
        assertThat(sessao.accessToken()).isEqualTo("access-" + alice.id());
        assertThat(sessao.refreshToken()).isEqualTo("opaco-1");
        assertThat(sessao.user().id()).isEqualTo(alice.id());
        assertThat(sessao.user().role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void deveEntrarComEmailEmMaiusculasEEspacos() {
        // O e-mail é normalizado antes da busca
        assertThat(entrar("  ALICE@Hemovia.com ", SENHA).user().id()).isEqualTo(alice.id());
    }

    @Test
    void deveEntrarComCpfMascarado() {
        // Quando ela entra com o CPF com pontos e traço
        assertThat(entrar("529.982.247-25", SENHA).user().id()).isEqualTo(alice.id());
    }

    @Test
    void deveEntrarComCpfSoComDigitos() {
        // Sem máscara também funciona
        assertThat(entrar("52998224725", SENHA).user().id()).isEqualTo(alice.id());
    }

    @Test
    void deveExpirarOAccessTokenEmQuinzeMinutos() {
        // O access token vale 15 minutos a partir de agora
        assertThat(entrar("alice@hemovia.com", SENHA).accessExpiresAt()).isEqualTo(AGORA.plus(Duration.ofMinutes(15)));
    }

    @Test
    void deveFalharComSenhaErrada() {
        assertThatThrownBy(() -> entrar("alice@hemovia.com", "senhaErrada"))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveFalharComUsuarioInexistente() {
        assertThatThrownBy(() -> entrar("naoexiste@hemovia.com", SENHA))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveFalharComIdentificadorQueNaoEhEmailNemCpf() {
        // Texto solto vira a mesma falha genérica, sem revelar que o formato estava errado
        assertThatThrownBy(() -> entrar("abc", SENHA))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveFalharComUsuarioDesativado() {
        // Dada uma conta desativada
        alice.deactivate();
        // Então nem a senha certa entra, e a mensagem é a mesma
        assertThatThrownBy(() -> entrar("alice@hemovia.com", SENHA))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveFalharComUsuarioAntigoSemPapel() {
        // Dado um usuário de antes dos papéis, restaurado com papel nulo
        cenario.usuarios.save(User.restore(UUID.randomUUID(), "Antigo", new Email("antigo@hemovia.com"),
                new Cpf("111.444.777-35"), null, "hash:" + SENHA, null, true, null, null, null));
        // Então ele não entra até um administrador atribuir um papel
        assertThatThrownBy(() -> entrar("antigo@hemovia.com", SENHA))
                .isInstanceOf(InvalidCredentialsException.class).hasMessage("Credenciais inválidas.");
    }

    @Test
    void deveGuardarSoOHashDoRefreshTokenNuncaOValorPuro() {
        // Quando ela entra
        SessionTokens sessao = entrar("alice@hemovia.com", SENHA);
        // Então existe um refresh token guardado, e o que está guardado é o resumo, não o valor entregue ao cliente
        assertThat(cenario.tokens.todos()).hasSize(1);
        RefreshToken guardado = cenario.tokens.todos().get(0);
        assertThat(guardado.tokenHash()).isEqualTo("sha:" + sessao.refreshToken()).isNotEqualTo(sessao.refreshToken());
        assertThat(guardado.userId()).isEqualTo(alice.id());
        assertThat(guardado.expiresAt()).isEqualTo(AGORA.plus(Duration.ofDays(7))).isEqualTo(sessao.refreshExpiresAt());
    }

    @Test
    void deveAbrirUmaFamiliaNovaACadaLogin() {
        // Dois logins são duas sessões independentes
        entrar("alice@hemovia.com", SENHA);
        entrar("alice@hemovia.com", SENHA);
        assertThat(cenario.tokens.todos()).extracting(RefreshToken::familyId).doesNotHaveDuplicates();
    }
}
