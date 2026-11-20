package com.hemovia.api.model.algorithm.graph;

import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.valueobject.GeoPoint;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RoutingGraph(List<Node> nodes, List<Edge> edges) {
    public RoutingGraph {
        if (nodes == null || edges == null || nodes.stream().anyMatch(java.util.Objects::isNull) || edges.stream().anyMatch(java.util.Objects::isNull)) {
            throw new InvalidRouteException("Grafo inválido.");
        }
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
        Set<UUID> nodeIds = new HashSet<>();
        for (Node node : nodes) {
            if (!nodeIds.add(node.id())) throw new InvalidRouteException("Vértice duplicado.");
        }
        Set<UUID> edgeIds = new HashSet<>();
        for (Edge edge : edges) {
            if (!edgeIds.add(edge.id()) || !nodeIds.contains(edge.from()) || !nodeIds.contains(edge.to())) {
                throw new InvalidRouteException("Ligação inválida no grafo.");
            }
        }
    }

    public record Node(UUID id, GeoPoint point) {
        public Node {
            if (id == null || point == null) throw new InvalidRouteException("Vértice inválido.");
        }
    }

    public record Edge(UUID id, UUID from, UUID to, BigDecimal distanceKm, long durationSeconds,
                       List<GeoPoint> geometry) {
        public Edge {
            if (id == null || from == null || to == null || distanceKm == null
                    || distanceKm.signum() <= 0 || durationSeconds <= 0
                    || geometry == null || geometry.stream().anyMatch(java.util.Objects::isNull)) {
                throw new InvalidRouteException("Ligação ou peso inválido.");
            }
            geometry = List.copyOf(geometry);
        }
    }
}

