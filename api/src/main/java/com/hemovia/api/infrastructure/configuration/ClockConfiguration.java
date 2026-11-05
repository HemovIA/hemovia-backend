package com.hemovia.api.infrastructure.configuration;

import com.hemovia.api.application.port.output.ClockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/*
Registra o relógio da aplicação e a porta que os casos de uso usam para consultá-lo.
*/
@Configuration
public class ClockConfiguration {

    // UTC porque o banco e as datas de validade já trabalham em UTC
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // Os casos de uso recebem a porta, nunca o Clock diretamente
    @Bean
    public ClockProvider clockProvider(Clock clock) {
        return new SystemClockProvider(clock);
    }
}
