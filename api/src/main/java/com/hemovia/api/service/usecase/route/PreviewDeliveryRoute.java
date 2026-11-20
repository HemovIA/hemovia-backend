package com.hemovia.api.service.usecase.route;

import com.hemovia.api.dto.request.CalculateRouteRequest;
import com.hemovia.api.dto.response.GeoJsonLineString;
import com.hemovia.api.dto.response.RoutePreviewResponse;
import com.hemovia.api.model.algorithm.graph.Dijkstra;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.repository.jpa.BloodCenterRepository;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class PreviewDeliveryRoute {
    private final CurrentUserProvider currentUser;
    private final BloodCenterRepository centers;
    private final CalculateDeliveryRoute calculate;

    public PreviewDeliveryRoute(CurrentUserProvider currentUser, BloodCenterRepository centers, CalculateDeliveryRoute calculate) {
        this.currentUser = currentUser;
        this.centers = centers;
        this.calculate = calculate;
    }

    @Transactional
    public RoutePreviewResponse execute(CalculateRouteRequest request) {
        var user = currentUser.current();
        if (!user.worksAtBloodCenter(user.bloodCenterId())) {
            throw new InvalidDataException("É necessário um vínculo com um hemocentro para calcular a rota.");
        }
        centers.findActiveForUse(user.bloodCenterId())
                .orElseThrow(() -> new ResourceNotFoundException("Hemocentro", user.bloodCenterId()));
        var path = calculate.execute(user.bloodCenterId(), request.hospitalId(), request.component(), request.deliveryDeadline());
        return new RoutePreviewResponse(path.nodeIds(), path.distanceKm(), path.durationSeconds(), geometry(path));
    }

    public static GeoJsonLineString geometry(Dijkstra.Path path) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (var edge : path.edges()) {
            for (var point : edge.geometry()) {
                var coordinate = List.of(point.longitude(), point.latitude());
                if (coordinates.isEmpty() || !coordinates.get(coordinates.size() - 1).equals(coordinate)) {
                    coordinates.add(coordinate);
                }
            }
        }
        return new GeoJsonLineString("LineString", coordinates);
    }
}

