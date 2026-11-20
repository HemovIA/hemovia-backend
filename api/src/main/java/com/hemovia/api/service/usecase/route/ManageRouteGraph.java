package com.hemovia.api.service.usecase.route;

import com.hemovia.api.dto.request.CreateRouteEdgeRequest;
import com.hemovia.api.dto.request.CreateRouteNodeRequest;
import com.hemovia.api.dto.response.GeoJsonLineString;
import com.hemovia.api.dto.response.RouteEdgeResponse;
import com.hemovia.api.dto.response.RouteNodeResponse;
import com.hemovia.api.dto.internal.NewRouteEdge;
import com.hemovia.api.dto.internal.StoredRouteEdge;
import com.hemovia.api.model.entity.RouteNode;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.model.exception.InvalidRouteException;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.model.valueobject.GeoPoint;
import com.hemovia.api.repository.jpa.BloodCenterRepository;
import com.hemovia.api.repository.jpa.HospitalRepository;
import com.hemovia.api.repository.port.RouteGraphStore;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.RoadRoutingProvider;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManageRouteGraph {
    private final RouteGraphStore store;
    private final BloodCenterRepository centers;
    private final HospitalRepository hospitals;
    private final RoadRoutingProvider routing;
    private final ClockProvider clock;

    public ManageRouteGraph(RouteGraphStore store, BloodCenterRepository centers, HospitalRepository hospitals,
                            RoadRoutingProvider routing, ClockProvider clock) {
        this.store = store;
        this.centers = centers;
        this.hospitals = hospitals;
        this.routing = routing;
        this.clock = clock;
    }

    public RouteNodeResponse createNode(CreateRouteNodeRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.name().length() > 255
                || (request.bloodCenterId() != null && request.hospitalId() != null)) {
            throw new InvalidDataException("Dados do ponto inválidos.");
        }
        Double latitude = request.latitude();
        Double longitude = request.longitude();
        if (request.bloodCenterId() != null || request.hospitalId() != null) {
            if (latitude != null || longitude != null) {
                throw new InvalidDataException("Coordenadas não são aceitas junto a um vínculo.");
            }
            if (request.bloodCenterId() != null) {
                var center = centers.findActiveForUse(request.bloodCenterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Hemocentro", request.bloodCenterId()));
                if (store.findNodeForBloodCenter(request.bloodCenterId()).isPresent()) {
                    throw new DuplicateResourceException("Hemocentro já vinculado a um ponto.");
                }
                latitude = center.getLatitude();
                longitude = center.getLongitude();
            } else {
                var hospital = hospitals.findById(request.hospitalId())
                        .orElseThrow(() -> new ResourceNotFoundException("Hospital", request.hospitalId()));
                if (store.findNodeForHospital(request.hospitalId()).isPresent()) {
                    throw new DuplicateResourceException("Hospital já vinculado a um ponto.");
                }
                latitude = hospital.getLatitude();
                longitude = hospital.getLongitude();
            }
        }
        if (latitude == null || longitude == null) {
            throw new InvalidDataException("Coordenadas são obrigatórias.");
        }
        try {
            new GeoPoint(latitude, longitude);
        } catch (InvalidRouteException e) {
            throw new InvalidDataException("Coordenadas inválidas.");
        }
        try {
            return nodeResponse(store.saveNode(new RouteNode(request.name(), latitude, longitude,
                    request.bloodCenterId(), request.hospitalId())));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Vínculo já cadastrado.");
        }
    }

    @Transactional(readOnly = true)
    public List<RouteNodeResponse> listNodes() {
        return store.listNodes().stream().map(ManageRouteGraph::nodeResponse).toList();
    }

    public RouteEdgeResponse createEdge(CreateRouteEdgeRequest request) {
        if (request == null || request.fromNodeId() == null || request.toNodeId() == null
                || request.fromNodeId().equals(request.toNodeId())) {
            throw new InvalidDataException("Pontos da ligação inválidos.");
        }
        var from = store.findNode(request.fromNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ponto", request.fromNodeId()));
        var to = store.findNode(request.toNodeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ponto", request.toNodeId()));
        requireActiveCenter(from);
        requireActiveCenter(to);
        if (store.edgeExists(request.fromNodeId(), request.toNodeId())) {
            throw new DuplicateResourceException("Ligação já cadastrada.");
        }
        var segment = routing.route(new GeoPoint(from.getLatitude(), from.getLongitude()),
                new GeoPoint(to.getLatitude(), to.getLongitude()));
        try {
            return edgeResponse(store.saveEdge(new NewRouteEdge(from.getId(), to.getId(), segment,
                    "OSRM", clock.now())));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Ligação já cadastrada.");
        }
    }

    @Transactional(readOnly = true)
    public List<RouteEdgeResponse> listEdges() {
        return store.listEdges().stream().map(ManageRouteGraph::edgeResponse).toList();
    }

    private void requireActiveCenter(RouteNode node) {
        if (node.getBloodCenterId() != null) {
            centers.findActiveForUse(node.getBloodCenterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hemocentro", node.getBloodCenterId()));
        }
    }

    private static RouteNodeResponse nodeResponse(RouteNode node) {
        return new RouteNodeResponse(node.getId(), node.getName(), node.getLatitude(), node.getLongitude(),
                node.getBloodCenterId(), node.getHospitalId());
    }

    private static RouteEdgeResponse edgeResponse(StoredRouteEdge edge) {
        var coordinates = edge.segment().geometry().stream()
                .map(point -> List.of(point.longitude(), point.latitude())).toList();
        return new RouteEdgeResponse(edge.id(), edge.fromNodeId(), edge.toNodeId(), edge.segment().distanceKm(),
                edge.segment().durationSeconds(), new GeoJsonLineString("LineString", coordinates),
                edge.provider(), edge.calculatedAt());
    }
}
