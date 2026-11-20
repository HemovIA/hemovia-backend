CREATE UNIQUE INDEX uk_transports_active_vehicle ON transports (vehicle_id) WHERE status = 'IN_TRANSIT';
