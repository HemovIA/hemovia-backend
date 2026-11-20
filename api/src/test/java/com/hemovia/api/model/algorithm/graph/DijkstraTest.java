package com.hemovia.api.model.algorithm.graph;

import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.valueobject.GeoPoint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {
    private static final GeoPoint POINT = new GeoPoint(0, 0);
    private static UUID id(long n) { return new UUID(0, n); }
    private static RoutingGraph.Node node(long n) { return new RoutingGraph.Node(id(n), POINT); }
    private static RoutingGraph.Edge edge(long n, long from, long to, String distance, long duration) {
        return new RoutingGraph.Edge(id(n), id(from), id(to), new BigDecimal(distance), duration, List.of(POINT, POINT));
    }

    @Test
    @DisplayName("@spec:AC-048 seleciona menor tempo acumulado, preservando trechos e somas")
    void findsFastestPathInsteadOfShorterDistanceOrImmediateNeighbor() {
        var a = edge(10, 1, 2, "1", 1);
        var b = edge(11, 2, 4, "1", 100);
        var c = edge(12, 1, 3, "3", 20);
        var d = edge(13, 3, 4, "4", 20);
        var cycle = edge(14, 3, 1, "1", 1);
        var graph = new RoutingGraph(List.of(node(1), node(2), node(3), node(4)), List.of(a, b, c, d, cycle));
        var path = Dijkstra.find(graph, id(1), id(4));
        assertEquals(List.of(id(1), id(3), id(4)), path.nodeIds());
        assertEquals(List.of(c, d), path.edges());
        assertEquals(new BigDecimal("7"), path.distanceKm());
        assertEquals(40, path.durationSeconds());
        assertThrows(UnsupportedOperationException.class, () -> path.nodeIds().clear());
    }

    @Test
    @DisplayName("@spec:AC-048 respeita sentido e rejeita pontos e conexões inexistentes")
    void rejectsUnreachableAndInvalidGraph() {
        var graph = new RoutingGraph(List.of(node(1), node(2)), List.of(edge(10, 1, 2, "1", 10)));
        assertThrows(InvalidRouteException.class, () -> Dijkstra.find(graph, id(2), id(1)));
        assertThrows(InvalidRouteException.class, () -> Dijkstra.find(graph, id(1), id(3)));
        assertThrows(InvalidRouteException.class, () -> new RoutingGraph(List.of(node(1)), List.of(edge(10, 1, 2, "1", 1))));
        assertThrows(InvalidRouteException.class, () -> edge(10, 1, 2, "0", 1));
        assertThrows(InvalidRouteException.class, () -> edge(10, 1, 2, "1", 0));
        assertThrows(InvalidRouteException.class, () -> new GeoPoint(Double.NaN, 0));
        assertEquals("INVALID_ROUTE", assertThrows(InvalidRouteException.class, () -> Dijkstra.find(graph, null, id(1))).code());
    }

    @Test
    @DisplayName("@spec:AC-048 mantém coleções imutáveis e evita transbordamento")
    void copiesCollectionsAndAvoidsOverflow() {
        var nodes = new ArrayList<>(List.of(node(1), node(2), node(3)));
        var graph = new RoutingGraph(nodes, List.of(edge(10, 1, 2, "1", Long.MAX_VALUE), edge(11, 2, 3, "1", 1)));
        nodes.clear();
        assertEquals(3, graph.nodes().size());
        assertThrows(InvalidRouteException.class, () -> Dijkstra.find(graph, id(1), id(3)));
        assertEquals(List.of(id(1)), Dijkstra.find(graph, id(1), id(1)).nodeIds());
    }
}
