package com.hemovia.api.repository.adapter;

import com.hemovia.api.dto.internal.NewRouteEdge;
import com.hemovia.api.dto.internal.StoredRouteEdge;
import com.hemovia.api.model.RoadSegment;
import com.hemovia.api.model.algorithm.graph.RoutingGraph;
import com.hemovia.api.model.entity.RouteEdge;
import com.hemovia.api.model.entity.RouteNode;
import com.hemovia.api.model.valueobject.GeoPoint;
import com.hemovia.api.repository.jpa.RouteEdgeRepository;
import com.hemovia.api.repository.jpa.RouteNodeRepository;
import com.hemovia.api.repository.mapper.RoadGeometryCodec;
import com.hemovia.api.repository.port.RouteGraphStore;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RouteGraphStoreAdapter implements RouteGraphStore {
    private final RouteNodeRepository nodes;
    private final RouteEdgeRepository edges;
    private final RoadGeometryCodec codec;

    public RouteGraphStoreAdapter(RouteNodeRepository nodes, RouteEdgeRepository edges, RoadGeometryCodec codec) {
        this.nodes = nodes;
        this.edges = edges;
        this.codec = codec;
    }

    public RouteNode saveNode(RouteNode node) {
        return nodes.saveAndFlush(node);
    }

    public StoredRouteEdge saveEdge(NewRouteEdge edge) {
        RoadSegment segment = edge.segment();
        RouteEdge entity = new RouteEdge(edge.fromNodeId(), edge.toNodeId(), segment.distanceKm(),
                segment.durationSeconds(), codec.encode(segment.geometry()), edge.provider(), edge.calculatedAt());
        return toStored(edges.saveAndFlush(entity));
    }

    public Optional<RouteNode> findNode(UUID id) {
        return nodes.findById(id);
    }

    public Optional<RouteNode> findNodeForBloodCenter(UUID id) {
        return nodes.findByBloodCenterId(id);
    }

    public Optional<RouteNode> findNodeForHospital(UUID id) {
        return nodes.findByHospitalId(id);
    }

    public boolean edgeExists(UUID from, UUID to) {
        return edges.existsByFromNodeIdAndToNodeId(from, to);
    }

    public List<RouteNode> listNodes() {
        return nodes.findAll();
    }

    public List<StoredRouteEdge> listEdges() {
        return edges.findAll().stream().filter(this::hasRealGeometry).map(this::toStored).toList();
    }

    public RoutingGraph loadGraph() {
        var graphNodes = nodes.findAll().stream()
                .map(node -> new RoutingGraph.Node(node.getId(), new GeoPoint(node.getLatitude(), node.getLongitude())))
                .toList();
        var graphEdges = edges.findAll().stream().filter(this::hasRealGeometry)
                .filter(edge -> !edge.isBidirectional())
                .map(edge -> new RoutingGraph.Edge(edge.getId(), edge.getFromNodeId(), edge.getToNodeId(),
                        edge.getDistanceKm(), edge.getTravelSeconds(), codec.decode(edge.getGeometryJson())))
                .toList();
        return new RoutingGraph(graphNodes, graphEdges);
    }

    private boolean hasRealGeometry(RouteEdge edge) {
        return edge.getGeometryJson() != null && edge.getProvider() != null && edge.getCalculatedAt() != null;
    }

    private StoredRouteEdge toStored(RouteEdge edge) {
        var segment = new RoadSegment(edge.getDistanceKm(), edge.getTravelSeconds(), codec.decode(edge.getGeometryJson()));
        return new StoredRouteEdge(edge.getId(), edge.getFromNodeId(), edge.getToNodeId(), segment,
                edge.getProvider(), edge.getCalculatedAt());
    }
}
