package com.hemovia.api.migration;

import com.hemovia.api.support.BancoDeMigracao;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class MigracaoV16TransportesTest {
    @Test @DisplayName("@spec:AC-060 @spec:AC-066 V16 preserva dados e impede viagens simultâneas no mesmo veículo")
    void ac060PreservesLegacyAndEnforcesOneActiveVehicle() throws Exception {
        try (BancoDeMigracao banco = new BancoDeMigracao()) {
            banco.migrarAte("15");
            UUID first = banco.criarTransporte(), second = banco.criarTransporte(), third = banco.criarTransporte();
            UUID vehicle = (UUID) banco.valor("select vehicle_id from transports where id=?", first);
            banco.executar("update transports set vehicle_id=? where id in (?,?)", vehicle, second, third);
            banco.migrarTudo();
            assertThat(banco.versaoAtual()).isEqualTo("16");
            assertThat(banco.contar("select count(*) from transports where vehicle_id=? and status='PLANNED'", vehicle)).isEqualTo(3);
            banco.exigirIndice("uk_transports_active_vehicle");
            banco.executar("update transports set status='IN_TRANSIT' where id=?", first);
            banco.exigirRestricao("uk_transports_active_vehicle", () -> banco.executar("update transports set status='IN_TRANSIT' where id=?", second));
            banco.executar("update transports set status='DELIVERED', completed_at=now() where id=?", first);
            banco.executar("update transports set status='IN_TRANSIT' where id=?", second);
            assertThat(banco.contar("select count(*) from transports where vehicle_id=?", vehicle)).isEqualTo(3);
        }
    }
}
