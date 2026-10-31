package com.hemovia.api.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/*
Ciclo 5 da Seção 3: o refresh token só serve enquanto não venceu e não foi revogado.
*/
class RefreshTokenTest {

    // Instante fixo da emissão, para o teste não depender do relógio
    private static final Instant EMISSAO = Instant.parse("2026-08-11T12:00:00Z");

    // Token emitido com validade de 7 dias
    private static RefreshToken emitir() {
        return RefreshToken.issue(UUID.randomUUID(), UUID.randomUUID(), "hash", EMISSAO, Duration.ofDays(7));
    }

    @Test
    void deveSerUtilDepoisDeEmitido() {
        // Um token novo vale no mesmo instante e antes de vencer
        RefreshToken token = emitir();
        assertThat(token.isUsableAt(EMISSAO)).isTrue();
        assertThat(token.isUsableAt(EMISSAO.plus(Duration.ofDays(6)))).isTrue();
    }

    @Test
    void deveCalcularAValidadePelaDuracaoInformada() {
        // A validade é a emissão mais os 7 dias
        assertThat(emitir().expiresAt()).isEqualTo(EMISSAO.plus(Duration.ofDays(7)));
    }

    @Test
    void deveDeixarDeValerNoInstanteDoVencimento() {
        // No instante exato do vencimento o token já não vale
        assertThat(emitir().isUsableAt(EMISSAO.plus(Duration.ofDays(7)))).isFalse();
        assertThat(emitir().isUsableAt(EMISSAO.plus(Duration.ofDays(8)))).isFalse();
    }

    @Test
    void deveDeixarDeValerDepoisDeRevogado() {
        // Dado um token revogado em seguida à emissão
        RefreshToken token = emitir();
        UUID substituto = UUID.randomUUID();
        token.revoke(EMISSAO.plusSeconds(60), substituto);
        // Então não vale mais, mesmo dentro da validade, e guarda quem o substituiu
        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isUsableAt(EMISSAO.plusSeconds(120))).isFalse();
        assertThat(token.replacedBy()).isEqualTo(substituto);
        assertThat(token.revokedAt()).isEqualTo(EMISSAO.plusSeconds(60));
    }

    @Test
    void devePermitirRevogarSemSubstituto() {
        // No logout não existe token novo
        RefreshToken token = emitir();
        token.revoke(EMISSAO, null);
        assertThat(token.isRevoked()).isTrue();
        assertThat(token.replacedBy()).isNull();
    }

    @Test
    void deveManterAPrimeiraRevogacaoQuandoRevogadoDeNovo() {
        // Revogar duas vezes não pode apagar o registro de quando e por quem foi revogado primeiro
        RefreshToken token = emitir();
        UUID primeiro = UUID.randomUUID();
        token.revoke(EMISSAO.plusSeconds(10), primeiro);
        token.revoke(EMISSAO.plusSeconds(99), UUID.randomUUID());
        assertThat(token.revokedAt()).isEqualTo(EMISSAO.plusSeconds(10));
        assertThat(token.replacedBy()).isEqualTo(primeiro);
    }
}
