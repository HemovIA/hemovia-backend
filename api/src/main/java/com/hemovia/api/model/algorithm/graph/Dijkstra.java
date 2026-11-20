package com.hemovia.api.model.algorithm.graph;

import com.hemovia.api.model.exception.InvalidRouteException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

public final class Dijkstra {
    private Dijkstra() { }

    public record Path(List<UUID> nodeIds, List<RoutingGraph.Edge> edges,
                       BigDecimal distanceKm, long durationSeconds) {
        public Path {
            nodeIds = List.copyOf(nodeIds);
            edges = List.copyOf(edges);
        }
    }

    private record Entry(UUID node, long duration) { }

    public static Path find(RoutingGraph graph, UUID origin, UUID destination) {
        if (graph == null || origin == null || destination == null) {
            throw new InvalidRouteException("Origem ou destino inválido.");
        }
        Map<UUID, List<RoutingGraph.Edge>> adjacency = new HashMap<>();
        for (RoutingGraph.Node node : graph.nodes()) adjacency.put(node.id(), new ArrayList<>());
        if (!adjacency.containsKey(origin) || !adjacency.containsKey(destination)) {
            throw new InvalidRouteException("Origem ou destino inexistente.");
        }
        for (RoutingGraph.Edge edge : graph.edges()) adjacency.get(edge.from()).add(edge);
        adjacency.values().forEach(list -> list.sort(Comparator.comparing(RoutingGraph.Edge::id)));
        Map<UUID, Long> durations = new HashMap<>();
        Map<UUID, RoutingGraph.Edge> previous = new HashMap<>();
        Set<UUID> settled = new HashSet<>();
        PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparingLong(Entry::duration).thenComparing(Entry::node));
        durations.put(origin, 0L);
        queue.add(new Entry(origin, 0));
        while (!queue.isEmpty()) {
            Entry current = queue.remove();
            if (!settled.add(current.node())) continue;
            if (current.node().equals(destination)) break;
            for (RoutingGraph.Edge edge : adjacency.get(current.node())) {
                if (settled.contains(edge.to()) || current.duration() > Long.MAX_VALUE - edge.durationSeconds()) continue;
                long candidate = current.duration() + edge.durationSeconds();
                Long known = durations.get(edge.to());
                if (known == null || candidate < known) {
                    durations.put(edge.to(), candidate);
                    previous.put(edge.to(), edge);
                    queue.add(new Entry(edge.to(), candidate));
                }
            }
        }
        if (!settled.contains(destination)) throw new InvalidRouteException("Não existe caminho viável entre os pontos.");
        List<UUID> nodes = new ArrayList<>();
        List<RoutingGraph.Edge> edges = new ArrayList<>();
        UUID cursor = destination;
        nodes.add(cursor);
        while (!cursor.equals(origin)) {
            RoutingGraph.Edge edge = previous.get(cursor);
            edges.add(edge);
            cursor = edge.from();
            nodes.add(cursor);
        }
        Collections.reverse(nodes);
        Collections.reverse(edges);
        BigDecimal distance = edges.stream().map(RoutingGraph.Edge::distanceKm).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Path(nodes, edges, distance, durations.get(destination));
    }
}
