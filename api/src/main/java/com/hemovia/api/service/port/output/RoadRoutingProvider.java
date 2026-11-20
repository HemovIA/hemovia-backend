package com.hemovia.api.service.port.output;

import com.hemovia.api.model.RoadSegment;
import com.hemovia.api.model.valueobject.GeoPoint;

public interface RoadRoutingProvider {
    RoadSegment route(GeoPoint from, GeoPoint to);
}
