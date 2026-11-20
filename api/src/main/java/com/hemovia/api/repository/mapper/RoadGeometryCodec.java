package com.hemovia.api.repository.mapper;

import com.hemovia.api.model.valueobject.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class RoadGeometryCodec {
    private final ObjectMapper mapper = new ObjectMapper();

    public String encode(List<GeoPoint> points) {
        return mapper.writeValueAsString(points);
    }

    public List<GeoPoint> decode(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            if (!root.isArray() || root.size() < 2) {
                throw new IllegalArgumentException("Geometria inválida");
            }
            List<GeoPoint> result = new ArrayList<>();
            for (JsonNode point : root) {
                if (!point.has("latitude") || !point.has("longitude")
                        || !point.get("latitude").isNumber() || !point.get("longitude").isNumber()) {
                    throw new IllegalArgumentException("Geometria inválida");
                }
                result.add(new GeoPoint(point.get("latitude").asDouble(), point.get("longitude").asDouble()));
            }
            return List.copyOf(result);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Geometria inválida", e);
        }
    }
}
