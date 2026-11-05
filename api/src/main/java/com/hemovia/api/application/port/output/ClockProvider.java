package com.hemovia.api.application.port.output;

import java.time.Instant;
import java.time.LocalDate;

/*
Porta de saída que entrega o "agora" para as regras de negócio.
Assim nenhuma regra chama o relógio do sistema direto, e os testes conseguem parar o tempo.
*/
public interface ClockProvider {

    // Momento exato de agora
    Instant now();

    // Data de hoje no fuso do relógio
    LocalDate today();
}
