CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    blood_bag_id UUID NOT NULL REFERENCES blood_bags(id),
    previous_status VARCHAR(255) NOT NULL,
    new_status VARCHAR(255) NOT NULL,
    movement_type VARCHAR(255) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    actor_id UUID REFERENCES users(id),
    reason VARCHAR(255),
    CONSTRAINT ck_stock_movements_previous_status CHECK (previous_status IN ('AVAILABLE', 'RESERVED', 'IN_TRANSIT', 'DELIVERED', 'DISCARDED')),
    CONSTRAINT ck_stock_movements_new_status CHECK (new_status IN ('AVAILABLE', 'RESERVED', 'IN_TRANSIT', 'DELIVERED', 'DISCARDED')),
    CONSTRAINT ck_stock_movements_type CHECK (movement_type IN ('ENTRY', 'RESERVATION', 'RELEASE', 'DISPATCH', 'DELIVERY', 'DISCARD'))
);

CREATE INDEX ix_stock_movements_blood_bag_occurred ON stock_movements(blood_bag_id, occurred_at);
CREATE INDEX ix_stock_movements_actor_id ON stock_movements(actor_id);
