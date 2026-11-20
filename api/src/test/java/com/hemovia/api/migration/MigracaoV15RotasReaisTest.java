package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class MigracaoV15RotasReaisTest {
    @Test @DisplayName("@spec:AC-052 V15 preserva dados legados e impõe FK e ordem do snapshot")
    void ac052UpgradesLegacyWithoutInventedRouteOrGeometry() throws Exception {
        try (BancoDeMigracao banco = new BancoDeMigracao()) {
            banco.migrarAte("14");
            UUID transport = banco.criarTransporte();
            UUID a = UUID.randomUUID(), b = UUID.randomUUID(), edge = UUID.randomUUID();
            banco.executar("insert into route_nodes(id,name,latitude,longitude) values (?,'A',0,0),(?,'B',0,1)", a, b);
            banco.executar("insert into route_edges(id,from_node_id,to_node_id,distance_km,travel_seconds) values (?,?,?,1,60)", edge, a, b);
            banco.migrarTudo();
            assertThat(banco.versaoAtual()).isEqualTo("15");
            assertThat(banco.contar("select count(*) from allocations where route_id is null")).isEqualTo(1);
            assertThat(banco.contar("select count(*) from route_edges where id=? and geometry_json is null and provider is null and calculated_at is null", edge)).isEqualTo(1);
            assertThat(banco.contar("select count(*) from delivery_route_nodes")).isZero();
            UUID route = (UUID) banco.valor("select route_id from transports where id=?", transport);
            banco.executar("insert into delivery_route_nodes(route_id,position,node_id) values (?,0,?),(?,1,?)", route, a, route, b);
            assertThat(banco.valor("select node_id from delivery_route_nodes where route_id=? order by position limit 1", route)).isEqualTo(a);
            banco.exigirRejeicao("delivery_route_nodes_pkey", () -> banco.executar("insert into delivery_route_nodes values (?,0,?)", route, b));
            banco.exigirRejeicao("foreign key", () -> banco.executar("update allocations set route_id=?", UUID.randomUUID()));
        }
    }
}
