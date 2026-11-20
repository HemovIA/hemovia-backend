package com.hemovia.api.service.routing;

import com.hemovia.api.model.RoadSegment;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.exception.RoutingProviderUnavailableException;
import com.hemovia.api.model.valueobject.GeoPoint;
import com.hemovia.api.service.config.RoutingProperties;
import com.hemovia.api.service.port.output.RoadRoutingProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class OsrmRoadRoutingProvider implements RoadRoutingProvider {
    private final RoutingProperties properties;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final long intervalNanos;
    private long lastCompletedCall;
    private boolean called;

    public OsrmRoadRoutingProvider(RoutingProperties properties) {
        this.properties = properties;
        if (properties.getConnectTimeout() == null || properties.getConnectTimeout().isZero()
                || properties.getConnectTimeout().isNegative() || properties.getRequestTimeout() == null
                || properties.getRequestTimeout().isZero() || properties.getRequestTimeout().isNegative()
                || properties.getMinimumInterval() == null || properties.getMinimumInterval().isNegative()) {
            throw new IllegalArgumentException("Configuração de roteamento inválida");
        }
        URI base = URI.create(properties.getBaseUrl());
        if (!("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getHost() == null) {
            throw new IllegalArgumentException("URL de roteamento inválida");
        }
        Duration interval = properties.getMinimumInterval();
        if ("router.project-osrm.org".equalsIgnoreCase(base.getHost())
                && interval.compareTo(Duration.ofSeconds(1)) < 0) {
            interval = Duration.ofSeconds(1);
        }
        intervalNanos = interval.toNanos();
        client = HttpClient.newBuilder().connectTimeout(properties.getConnectTimeout()).build();
    }

    @Override
    public synchronized RoadSegment route(GeoPoint from, GeoPoint to) {
        try {
            throttle();
            HttpRequest request = HttpRequest.newBuilder(routeUri(from, to))
                    .timeout(properties.getRequestTimeout())
                    .header("User-Agent", "Hemovia/1.0")
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 400) {
                throw unavailable();
            }
            JsonNode root = mapper.readTree(response.body());
            String code = root.path("code").asText();
            if ("NoRoute".equals(code) || "NoSegment".equals(code)) {
                throw new InvalidRouteException("Não há rota viária entre os pontos.");
            }
            if (response.statusCode() != 200 || !"Ok".equals(code)) {
                throw unavailable();
            }
            return parseRoute(root);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable();
        } catch (InvalidRouteException | RoutingProviderUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw unavailable();
        } finally {
            lastCompletedCall = System.nanoTime();
            called = true;
        }
    }

    private URI routeUri(GeoPoint from, GeoPoint to) {
        String base = properties.getBaseUrl().replaceAll("/+$", "");
        return URI.create(base + "/route/v1/driving/" + from.longitude() + "," + from.latitude()
                + ";" + to.longitude() + "," + to.latitude()
                + "?overview=full&geometries=geojson&steps=false");
    }

    private void throttle() throws InterruptedException {
        if (called) {
            long wait = intervalNanos - (System.nanoTime() - lastCompletedCall);
            if (wait > 0) {
                Thread.sleep(wait / 1_000_000, (int) (wait % 1_000_000));
            }
        }
    }

    private RoadSegment parseRoute(JsonNode root) {
        JsonNode routes = root.path("routes");
        if (!routes.isArray() || routes.isEmpty()) {
            throw unavailable();
        }
        JsonNode route = routes.get(0);
        JsonNode distance = route.path("distance");
        JsonNode duration = route.path("duration");
        JsonNode geometry = route.path("geometry");
        JsonNode coordinates = geometry.path("coordinates");
        if (!distance.isNumber() || !duration.isNumber()
                || !Double.isFinite(distance.asDouble()) || !Double.isFinite(duration.asDouble())
                || distance.asDouble() <= 0 || duration.asDouble() <= 0
                || !"LineString".equals(geometry.path("type").asText())
                || !coordinates.isArray() || coordinates.size() < 2) {
            throw unavailable();
        }
        List<GeoPoint> points = new ArrayList<>();
        for (JsonNode point : coordinates) {
            if (!point.isArray() || point.size() != 2
                    || !point.get(0).isNumber() || !point.get(1).isNumber()) {
                throw unavailable();
            }
            try {
                points.add(new GeoPoint(point.get(1).asDouble(), point.get(0).asDouble()));
            } catch (InvalidRouteException e) {
                throw unavailable();
            }
        }
        BigDecimal km = distance.decimalValue().divide(BigDecimal.valueOf(1000), 3, RoundingMode.CEILING);
        long seconds = duration.decimalValue().setScale(0, RoundingMode.CEILING).longValueExact();
        if (km.signum() <= 0 || seconds <= 0) {
            throw unavailable();
        }
        return new RoadSegment(km, seconds, points);
    }

    private static RoutingProviderUnavailableException unavailable() {
        return new RoutingProviderUnavailableException("Serviço de roteamento indisponível ou resposta inválida.");
    }
}
