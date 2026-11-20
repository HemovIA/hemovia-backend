ALTER TABLE route_edges ADD COLUMN geometry_json TEXT, ADD COLUMN provider VARCHAR(255), ADD COLUMN calculated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE allocations ADD COLUMN route_id UUID REFERENCES delivery_routes(id);
CREATE TABLE delivery_route_nodes (route_id UUID NOT NULL REFERENCES delivery_routes(id), position INTEGER NOT NULL, node_id UUID NOT NULL, PRIMARY KEY (route_id, position));
