package com.hemovia.api.infrastructure.configuration;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/*
Confere o relógio injetado: nenhuma regra olha para o relógio do sistema direto,
tudo passa pelo ClockProvider para os testes poderem parar o tempo.
*/
class SystemClockProviderTest {

    @Test
    void deveDevolverOInstanteDoRelogio() {
        // Dado um relógio parado em um instante conhecido
        Instant instante = Instant.parse("2026-08-11T15:30:00Z");
        SystemClockProvider provedor = new SystemClockProvider(Clock.fixed(instante, ZoneOffset.UTC));
        // Quando pedimos o agora, então recebemos exatamente esse instante
        assertThat(provedor.now()).isEqualTo(instante);
    }

    @Test
    void deveCalcularAdataNoFusoDoRelogio() {
        // Dado um relógio em 02h UTC do dia 12, que ainda é dia 11 no Recife (UTC-3)
        Clock relogio = Clock.fixed(Instant.parse("2026-08-12T02:00:00Z"), ZoneId.of("America/Recife"));
        SystemClockProvider provedor = new SystemClockProvider(relogio);
        // Quando pedimos o hoje, então vale o fuso do relógio e não o do computador
        assertThat(provedor.today()).isEqualTo(LocalDate.of(2026, 8, 11));
    }

    @Test
    void deveConfigurarORelogioDaAplicacaoEmUtc() {
        // Quando a configuração cria o relógio, então ele roda em UTC como as datas do banco
        assertThat(new ClockConfiguration().clock().getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
