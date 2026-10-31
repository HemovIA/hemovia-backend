package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.RefreshTokenRepository;
import com.hemovia.api.application.port.output.UserRepository;
import com.hemovia.api.domain.enums.Role;
import com.hemovia.api.domain.model.RefreshToken;
import com.hemovia.api.domain.model.User;
import com.hemovia.api.domain.valueobject.Cpf;
import com.hemovia.api.domain.valueobject.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 24 da Seção 3 (refresh tokens): salvar, buscar pelo hash e revogar por família ou por usuário, no PostgreSQL real.
Cada teste roda numa transação que é desfeita no fim.
*/
@Tag("integration")
@SpringBootTest
@Transactional
class RefreshTokenRepositoryAdapterIntegrationTest {

    // Instante fixo da emissão, para o teste não depender do relógio
    private static final Instant AGORA = Instant.parse("2026-08-11T12:00:00Z");

    @Autowired
    private RefreshTokenRepository tokens;

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private JdbcTemplate jdbc;

    private User dono;

    @BeforeEach
    void criarDono() {
        // O refresh token tem chave estrangeira para o usuário, então o dono precisa existir
        String sufixo = UUID.randomUUID().toString();
        String cpf = "529.982.247-25";
        // Se o CPF fixo já existir no banco de desenvolvimento, o teste troca por outro válido
        boolean existe = Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM users WHERE cpf = '52998224725')", Boolean.class));
        if (existe) {
            cpf = "111.444.777-35";
        }
        dono = usuarios.save(User.create("Alice", new Email(sufixo + "@hemovia.com"), new Cpf(cpf), null, "hash", Role.ADMIN, null));
    }

    // Emite um token da família informada com o hash informado
    private RefreshToken emitir(UUID familia, String hash) {
        return tokens.save(RefreshToken.issue(dono.id(), familia, hash + UUID.randomUUID(), AGORA, Duration.ofDays(7)));
    }

    @Test
    void deveGuardarEBuscarPeloHash() {
        // Quando guardamos um token
        RefreshToken salvo = emitir(UUID.randomUUID(), "abc");
        // Então ele volta pelo hash com todos os dados
        RefreshToken lido = tokens.findByTokenHash(salvo.tokenHash()).orElseThrow();
        assertThat(lido.id()).isEqualTo(salvo.id());
        assertThat(lido.userId()).isEqualTo(dono.id());
        assertThat(lido.familyId()).isEqualTo(salvo.familyId());
        assertThat(lido.expiresAt()).isEqualTo(AGORA.plus(Duration.ofDays(7)));
        assertThat(lido.isRevoked()).isFalse();
    }

    @Test
    void naoDeveAcharHashDesconhecido() {
        assertThat(tokens.findByTokenHash("hash-que-nunca-existiu")).isEmpty();
    }

    @Test
    void deveGuardarARevogacaoComOSubstituto() {
        // Dado um token antigo e o novo que o substitui
        UUID familia = UUID.randomUUID();
        RefreshToken antigo = emitir(familia, "antigo");
        RefreshToken novo = emitir(familia, "novo");
        // Quando o antigo é revogado apontando para o novo
        antigo.revoke(AGORA.plusSeconds(30), novo.id());
        tokens.save(antigo);
        // Então a revogação e o substituto ficam guardados
        RefreshToken lido = tokens.findByTokenHash(antigo.tokenHash()).orElseThrow();
        assertThat(lido.revokedAt()).isEqualTo(AGORA.plusSeconds(30));
        assertThat(lido.replacedBy()).isEqualTo(novo.id());
    }

    @Test
    void deveRevogarSoOsTokensDaFamiliaInformada() {
        // Dados dois tokens da família A e um da família B
        UUID familiaA = UUID.randomUUID();
        RefreshToken a1 = emitir(familiaA, "a1");
        RefreshToken a2 = emitir(familiaA, "a2");
        RefreshToken b1 = emitir(UUID.randomUUID(), "b1");
        // Quando a família A é revogada
        tokens.revokeFamily(familiaA, AGORA.plusSeconds(60));
        // Então os da A ficam revogados e o da B continua valendo
        assertThat(tokens.findByTokenHash(a1.tokenHash()).orElseThrow().revokedAt()).isEqualTo(AGORA.plusSeconds(60));
        assertThat(tokens.findByTokenHash(a2.tokenHash()).orElseThrow().isRevoked()).isTrue();
        assertThat(tokens.findByTokenHash(b1.tokenHash()).orElseThrow().isRevoked()).isFalse();
    }

    @Test
    void naoDeveSobrescreverARevogacaoQueJaExistia() {
        // Dado um token revogado antes
        UUID familia = UUID.randomUUID();
        RefreshToken token = emitir(familia, "x");
        token.revoke(AGORA.plusSeconds(10), null);
        tokens.save(token);
        // Quando a família é revogada depois
        tokens.revokeFamily(familia, AGORA.plusSeconds(99));
        // Então vale o momento da primeira revogação
        assertThat(tokens.findByTokenHash(token.tokenHash()).orElseThrow().revokedAt()).isEqualTo(AGORA.plusSeconds(10));
    }

    @Test
    void deveRevogarTodosOsTokensDeUmUsuario() {
        // Dado um usuário com duas sessões
        RefreshToken sessao1 = emitir(UUID.randomUUID(), "s1");
        RefreshToken sessao2 = emitir(UUID.randomUUID(), "s2");
        // Quando todos os tokens dele são revogados
        tokens.revokeAllOfUser(dono.id(), AGORA.plusSeconds(5));
        // Então as duas sessões ficam revogadas
        assertThat(tokens.findByTokenHash(sessao1.tokenHash()).orElseThrow().isRevoked()).isTrue();
        assertThat(tokens.findByTokenHash(sessao2.tokenHash()).orElseThrow().isRevoked()).isTrue();
    }
}
