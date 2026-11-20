package com.hemovia.api.service.usecase.route;

import com.hemovia.api.model.algorithm.graph.DeliveryWindow;
import com.hemovia.api.model.algorithm.graph.Dijkstra;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.repository.port.RouteGraphStore;
import com.hemovia.api.service.config.RoutingProperties;
import com.hemovia.api.service.port.output.ClockProvider;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class CalculateDeliveryRoute {
    private final RouteGraphStore graph;
    private final ClockProvider clock;
    private final RoutingProperties properties;

    public CalculateDeliveryRoute(RouteGraphStore graph, ClockProvider clock, RoutingProperties properties) {
        this.graph = graph;
        this.clock = clock;
        this.properties = properties;
    }

    public Dijkstra.Path execute(UUID centerId, UUID hospitalId, BloodComponent component, Instant deadline) {
        var origin = graph.findNodeForBloodCenter(centerId)
                .orElseThrow(() -> new InvalidRouteException("Hemocentro sem ponto na malha."));
        var destination = graph.findNodeForHospital(hospitalId)
                .orElseThrow(() -> new InvalidRouteException("Hospital sem ponto na malha."));
        var path = Dijkstra.find(graph.loadGraph(), origin.getId(), destination.getId());
        DeliveryWindow.validate(component, deadline, clock.now(), path.durationSeconds(), properties.getPlateletsMaxSeconds());
        return path;
    }
}
