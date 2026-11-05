package com.hemovia.api.infrastructure.configuration;

import com.hemovia.api.application.port.output.ClockProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

/*
Implementa o ClockProvider em cima de um java.time.Clock.
Em produção o Clock é o do sistema; nos testes é um relógio parado.
*/
public final class SystemClockProvider implements ClockProvider {

    // O relógio que fornece instante e fuso
    private final Clock clock;

    public SystemClockProvider(Clock clock) {
        // Guardamos o relógio recebido; é ele que decide o que é "agora"
        this.clock = clock;
    }

    @Override
    public Instant now() {
        // O instante vem direto do relógio injetado
        return clock.instant();
    }

    @Override
    public LocalDate today() {
        // A data sai do mesmo relógio, então respeita o fuso dele
        return LocalDate.now(clock);
    }
}
