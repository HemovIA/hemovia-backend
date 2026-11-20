package com.hemovia.api.repository.port;

import com.hemovia.api.dto.internal.NewRouteEdge;
import com.hemovia.api.dto.internal.StoredRouteEdge;
import com.hemovia.api.model.algorithm.graph.RoutingGraph;
import com.hemovia.api.model.entity.RouteNode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteGraphStore {
    RouteNode saveNode(RouteNode node);
    StoredRouteEdge saveEdge(NewRouteEdge edge);
    Optional<RouteNode> findNode(UUID id);
    Optional<RouteNode> findNodeForBloodCenter(UUID id);
    Optional<RouteNode> findNodeForHospital(UUID id);
    boolean edgeExists(UUID from, UUID to);
    List<RouteNode> listNodes();
    List<StoredRouteEdge> listEdges();
    RoutingGraph loadGraph();
}
