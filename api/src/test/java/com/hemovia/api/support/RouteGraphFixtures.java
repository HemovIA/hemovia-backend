package com.hemovia.api.support;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public final class RouteGraphFixtures {
    private RouteGraphFixtures() { }

    public static UUID center(JdbcTemplate jdbc, UUID center) {
        UUID node = UUID.randomUUID();
        jdbc.update("insert into route_nodes(id,name,latitude,longitude,blood_center_id) values (?,'center',0,0,?)", node, center);
        return node;
    }

    public static UUID hospital(JdbcTemplate jdbc, UUID hospital) {
        UUID node = UUID.randomUUID();
        jdbc.update("insert into route_nodes(id,name,latitude,longitude,hospital_id) values (?,'hospital',0,1,?)", node, hospital);
        return node;
    }

    public static UUID junction(JdbcTemplate jdbc, double latitude, double longitude) {
        UUID node = UUID.randomUUID();
        jdbc.update("insert into route_nodes(id,name,latitude,longitude) values (?,'junction',?,?)", node, latitude, longitude);
        return node;
    }

    public static void edge(JdbcTemplate jdbc, UUID from, UUID to, long seconds) {
        var first = jdbc.queryForMap("select latitude,longitude from route_nodes where id=?", from);
        var last = jdbc.queryForMap("select latitude,longitude from route_nodes where id=?", to);
        String geometry = "[{\"latitude\":" + first.get("latitude") + ",\"longitude\":" + first.get("longitude")
                + "},{\"latitude\":" + last.get("latitude") + ",\"longitude\":" + last.get("longitude") + "}]";
        jdbc.update("insert into route_edges(id,from_node_id,to_node_id,distance_km,travel_seconds,bidirectional,geometry_json,provider,calculated_at) values (?,?,?,1,?,false,?,'OSRM',now())",
                UUID.randomUUID(), from, to, seconds, geometry);
    }

    public static void cleanup(JdbcTemplate jdbc, UUID... nodes) {
        for (UUID node : nodes) {
            jdbc.update("delete from route_edges where from_node_id=? or to_node_id=?", node, node);
        }
        for (UUID node : nodes) jdbc.update("delete from route_nodes where id=?", node);
    }
}
