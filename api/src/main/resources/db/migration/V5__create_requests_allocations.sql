CREATE TABLE blood_requests (
    id UUID PRIMARY KEY,
    hospital_id UUID NOT NULL REFERENCES hospital(id),
    blood_type VARCHAR(255) NOT NULL,
    rh_factor VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_blood_requests_type CHECK (blood_type IN ('A', 'B', 'AB', 'O')),
    CONSTRAINT ck_blood_requests_rh_factor CHECK (rh_factor IN ('POSITIVE', 'NEGATIVE')),
    CONSTRAINT ck_blood_requests_component CHECK (component IN ('WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLASMA', 'PLATELETS', 'CRYOPRECIPITATE')),
    CONSTRAINT ck_blood_requests_quantity CHECK (quantity > 0),
    CONSTRAINT ck_blood_requests_status CHECK (status IN ('PENDING', 'ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE allocations (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL REFERENCES blood_requests(id),
    blood_center_id UUID NOT NULL REFERENCES blood_centers(id),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_allocations_status CHECK (status IN ('ALLOCATED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED'))
);

CREATE TABLE allocation_items (
    id UUID PRIMARY KEY,
    allocation_id UUID NOT NULL REFERENCES allocations(id),
    blood_bag_id UUID NOT NULL REFERENCES blood_bags(id),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    released_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_allocation_items_allocation_bag UNIQUE (allocation_id, blood_bag_id),
    CONSTRAINT ck_allocation_items_status CHECK (status IN ('RESERVED', 'IN_TRANSIT', 'DELIVERED', 'RELEASED'))
);

-- A released item remains history. Only reservations still consuming stock are unique.
CREATE UNIQUE INDEX uk_allocation_items_active_blood_bag
    ON allocation_items(blood_bag_id)
    WHERE status IN ('RESERVED', 'IN_TRANSIT');

CREATE INDEX ix_blood_requests_hospital_id ON blood_requests(hospital_id);
CREATE INDEX ix_allocations_request_id ON allocations(request_id);
CREATE INDEX ix_allocations_blood_center_id ON allocations(blood_center_id);
CREATE INDEX ix_allocation_items_allocation_id ON allocation_items(allocation_id);
