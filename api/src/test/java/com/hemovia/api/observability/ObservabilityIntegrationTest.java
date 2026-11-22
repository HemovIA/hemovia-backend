package com.hemovia.api.observability;

import com.hemovia.api.service.observability.BusinessMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservabilityIntegrationTest {
    @Test
    @DisplayName("@spec:AC-116 contadores avançam somente após commit e não no rollback")
    void businessMetricsFollowTransactionOutcome() {
        var registry = new SimpleMeterRegistry();
        var metrics = new BusinessMetrics(registry);
        var tx = new TransactionTemplate(new TestTransactionManager());
        tx.executeWithoutResult(status -> {
            metrics.donationRegistered();
            metrics.requestIssued();
            assertThat(registry.get("hemovia.doacoes.registradas").counter().count()).isZero();
            assertThat(registry.get("hemovia.requisicoes.emitidas").counter().count()).isZero();
        });
        tx.executeWithoutResult(status -> {
            metrics.donationRegistered();
            metrics.requestIssued();
            status.setRollbackOnly();
        });
        assertThat(registry.get("hemovia.doacoes.registradas").counter().count()).isEqualTo(1);
        assertThat(registry.get("hemovia.requisicoes.emitidas").counter().count()).isEqualTo(1);
        assertThatThrownBy(metrics::donationRegistered).isInstanceOf(IllegalStateException.class);
    }

    @org.junit.jupiter.api.Nested
    @org.springframework.boot.test.context.SpringBootTest(webEnvironment = org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT,
            properties = {"management.server.port=0", "spring.config.location=classpath:/application.yaml"})
    @org.junit.jupiter.api.Tag("integration")
    class HttpSecurityIntegration {
        @org.springframework.boot.test.web.server.LocalServerPort
        int port;

        @Test
        @DisplayName("@spec:AC-115 readiness e seguranca na porta principal")
        void mainPortProtectsBusinessRoutes() throws Exception {
            var client = java.net.http.HttpClient.newHttpClient();
            var ready = get(client, "/readyz");
            assertThat(ready.statusCode()).isEqualTo(200);
            assertThat(ready.body()).isEqualTo("{\"status\":\"UP\"}");
            assertThat(get(client, "/actuator/health").statusCode()).isNotEqualTo(200);
            assertThat(get(client, "/api/v1/donors").statusCode()).isIn(401, 403);
        }

        private java.net.http.HttpResponse<String> get(java.net.http.HttpClient client, String path) throws Exception {
            var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + path)).GET().build();
            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        }
    }

    private static class TestTransactionManager extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
