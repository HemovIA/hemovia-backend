package com.hemovia.api.service.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class BusinessMetrics {
    private final Counter donations;
    private final Counter requests;

    public BusinessMetrics(MeterRegistry registry) {
        donations = Counter.builder("hemovia.doacoes.registradas")
                .description("Doações registradas em transações confirmadas").register(registry);
        requests = Counter.builder("hemovia.requisicoes.emitidas")
                .description("Requisições de sangue emitidas em transações confirmadas").register(registry);
    }

    public void donationRegistered() { afterCommit(donations); }
    public void requestIssued() { afterCommit(requests); }

    private static void afterCommit(Counter counter) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Business metrics require an active synchronized transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { counter.increment(); }
        });
    }
}
