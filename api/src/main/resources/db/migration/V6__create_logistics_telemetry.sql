CREATE TABLE delivery_routes (
    id UUID PRIMARY KEY,
    origin_id UUID NOT NULL REFERENCES blood_centers(id),
    destination_id UUID NOT NULL REFERENCES hospital(id),
    distance_km NUMERIC(12,3) NOT NULL,
    estimated_duration_seconds BIGINT NOT NULL,
    CONSTRAINT ck_delivery_routes_distance CHECK (distance_km >= 0 AND distance_km NOT IN ('NaN'::numeric, 'Infinity'::numeric, '-Infinity'::numeric)),
    CONSTRAINT ck_delivery_routes_duration CHECK (estimated_duration_seconds >= 0)
);

CREATE TABLE route_waypoints (
    route_id UUID NOT NULL REFERENCES delivery_routes(id),
    position INTEGER NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_route_waypoints PRIMARY KEY (route_id, position),
    CONSTRAINT uk_route_waypoints_route_position UNIQUE (route_id, position),
    CONSTRAINT ck_route_waypoints_position CHECK (position >= 0),
    CONSTRAINT ck_route_waypoints_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_route_waypoints_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE transports (
    id UUID PRIMARY KEY,
    allocation_id UUID NOT NULL UNIQUE REFERENCES allocations(id),
    route_id UUID NOT NULL REFERENCES delivery_routes(id),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    status VARCHAR(255) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_transports_status CHECK (status IN ('PLANNED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT ck_transports_times CHECK (completed_at IS NULL OR completed_at >= started_at)
);

CREATE TABLE telemetry_readings (
    id UUID PRIMARY KEY,
    transport_id UUID NOT NULL REFERENCES transports(id),
    sensor_id VARCHAR(255) NOT NULL,
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    temperature_celsius NUMERIC(8,3) NOT NULL,
    CONSTRAINT ck_telemetry_sensor_not_blank CHECK (btrim(sensor_id) <> ''),
    CONSTRAINT ck_telemetry_temperature_finite CHECK (temperature_celsius NOT IN ('NaN'::numeric, 'Infinity'::numeric, '-Infinity'::numeric))
);

CREATE INDEX ix_delivery_routes_origin_id ON delivery_routes(origin_id);
CREATE INDEX ix_delivery_routes_destination_id ON delivery_routes(destination_id);
CREATE INDEX ix_transports_route_id ON transports(route_id);
CREATE INDEX ix_transports_vehicle_id ON transports(vehicle_id);
CREATE INDEX ix_telemetry_readings_transport_measured ON telemetry_readings(transport_id, measured_at);
