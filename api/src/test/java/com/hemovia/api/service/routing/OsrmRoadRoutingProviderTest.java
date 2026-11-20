package com.hemovia.api.service.routing;

import com.hemovia.api.model.RoadSegment;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.exception.RoutingProviderUnavailableException;
import com.hemovia.api.model.valueobject.GeoPoint;
import com.hemovia.api.service.config.RoutingProperties;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsrmRoadRoutingProviderTest {
    private HttpServer server;
    private OsrmRoadRoutingProvider provider;
    private final AtomicReference<String> body = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> agent = new AtomicReference<>();
    private final List<Long> receivedAt = new CopyOnWriteArrayList<>();
    private int status;

    @BeforeEach
    void setup() throws Exception {
        status = 200;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            receivedAt.add(System.nanoTime());
            path.set(exchange.getRequestURI().toString());
            agent.set(exchange.getRequestHeaders().getFirst("User-Agent"));
            byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        provider = createProvider(Duration.ofSeconds(2));
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private OsrmRoadRoutingProvider createProvider(Duration timeout) {
        return createProvider(timeout, Duration.ZERO);
    }

    private OsrmRoadRoutingProvider createProvider(Duration timeout, Duration interval) {
        RoutingProperties properties = new RoutingProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setMinimumInterval(interval);
        properties.setRequestTimeout(timeout);
        return new OsrmRoadRoutingProvider(properties);
    }

    private RoadSegment route() {
        return provider.route(new GeoPoint(-8.06, -34.9), new GeoPoint(-8.05, -34.88));
    }

    @Test
    @DisplayName("@spec:AC-050 consultas concorrentes respeitam intervalo de um segundo")
    void limitaConsultasConcorrentesAoIntervaloConfigurado() throws Exception {
        body.set("{\"code\":\"Ok\",\"routes\":[{\"distance\":1000,\"duration\":60,"
                + "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-34.9,-8.06],[-34.88,-8.05]]}}]}");
        provider = createProvider(Duration.ofSeconds(2), Duration.ofSeconds(1));
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> { start.await(); return route(); });
            var second = pool.submit(() -> { start.await(); return route(); });
            start.countDown();
            assertEquals(60, first.get(5, TimeUnit.SECONDS).durationSeconds());
            assertEquals(60, second.get(5, TimeUnit.SECONDS).durationSeconds());
            assertEquals(2, receivedAt.size());
            assertTrue(receivedAt.get(1) - receivedAt.get(0) >= Duration.ofSeconds(1).toNanos());
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    @DisplayName("@spec:AC-050 consulta e retorna geometria e duração")
    void consultaERetornaGeometriaEmOrdemComDuracaoArredondada() {
        body.set("{\"code\":\"Ok\",\"routes\":[{\"distance\":3866.6,\"duration\":348.1,"
                + "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-34.9,-8.06],[-34.88,-8.05]]}}]}");
        var result = route();
        assertEquals("3.867", result.distanceKm().toPlainString());
        assertEquals(349, result.durationSeconds());
        assertEquals(new GeoPoint(-8.06, -34.9), result.geometry().get(0));
        assertEquals("Hemovia/1.0", agent.get());
        assertEquals("/route/v1/driving/-34.9,-8.06;-34.88,-8.05?overview=full&geometries=geojson&steps=false", path.get());
    }

    @Test
    @DisplayName("@spec:AC-050 rota ou segmento ausente")
    void rotaOuSegmentoAusenteEhErroDeRota() {
        status = 400;
        for (String code : new String[]{"NoRoute", "NoSegment"}) {
            body.set("{\"code\":\"" + code + "\"}");
            assertThrows(InvalidRouteException.class, this::route);
        }
    }

    @Test
    @DisplayName("@spec:AC-050 resposta inválida e falha HTTP")
    void rejeitaRespostaInvalidaEHttpDeErro() {
        body.set("{\"code\":\"Ok\",\"routes\":[{\"distance\":1,\"duration\":1,"
                + "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[1,2]]}}]}");
        assertThrows(RoutingProviderUnavailableException.class, this::route);
        for (int error : new int[]{401, 429, 500, 502}) {
            status = error;
            body.set("{\"code\":\"NoRoute\"}");
            assertThrows(RoutingProviderUnavailableException.class, this::route);
        }
        body.set("resposta inválida");
        status = 200;
        assertThrows(RoutingProviderUnavailableException.class, this::route);
    }

    @Test
    @DisplayName("@spec:AC-050 timeout do provedor")
    void timeoutEhIndisponibilidade() throws Exception {
        server.stop(0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        server.start();
        provider = createProvider(Duration.ofMillis(50));
        assertThrows(RoutingProviderUnavailableException.class, this::route);
    }
}
