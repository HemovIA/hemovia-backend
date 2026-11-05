package com.hemovia.api.infrastructure.security;

import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.exception.InvalidCredentialsException;
import com.hemovia.api.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Ciclo 23 da Seção 3, mais os adaptadores pequenos de segurança: hash de senha, resumo SHA-256,
token aleatório e usuário atual lido do contexto do Spring Security.
*/
class AdaptadoresDeSegurancaTest {

    @AfterEach
    void limparContexto() {
        // Nenhum teste deixa usuário logado para o seguinte
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveConferirSenhaCertaERecusarErrada() {
        // Dado um hash BCrypt de uma senha
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        String hash = hasher.hash("senha123");
        // Então ele não é a senha pura e só confere com a senha certa
        assertThat(hash).isNotEqualTo("senha123");
        assertThat(hasher.matches("senha123", hash)).isTrue();
        assertThat(hasher.matches("senhaErrada", hash)).isFalse();
    }

    @Test
    void deveGerarHashesDiferentesParaAMesmaSenha() {
        // Cada hash leva um sal próprio
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        assertThat(hasher.hash("senha123")).isNotEqualTo(hasher.hash("senha123"));
    }

    @Test
    void deveCalcularSha256EmHexadecimal() {
        // O resumo SHA-256 de "abc" é um valor conhecido
        assertThat(new Sha256TokenHasher().sha256("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void deveGerarTokensOpacosDe43CaracteresSemRepetir() {
        // 32 bytes em Base64 URL sem preenchimento dão 43 caracteres seguros para cookie
        SecureRandomOpaqueTokenGenerator gerador = new SecureRandomOpaqueTokenGenerator();
        String primeiro = gerador.newToken();
        assertThat(primeiro).matches("[A-Za-z0-9_-]{43}");
        assertThat(gerador.newToken()).isNotEqualTo(primeiro);
    }

    @Test
    void deveLerOUsuarioAutenticadoDoContextoDeSeguranca() {
        // Dado um usuário autenticado no contexto
        AuthenticatedUser usuario = new AuthenticatedUser(UUID.randomUUID(), Role.ADMIN, null, null, null);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
        // Então o provedor devolve exatamente ele
        assertThat(new SecurityContextCurrentUserProvider().current()).isEqualTo(usuario);
    }

    @Test
    void deveFalharQuandoNaoHaNingemAutenticado() {
        assertThatThrownBy(() -> new SecurityContextCurrentUserProvider().current()).isInstanceOf(InvalidCredentialsException.class);
    }
}
