package com.hemovia.api.model;
import com.hemovia.api.support.BaseEntitiesPostgres;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class DonorMigrationIT {
    @Test @DisplayName("@spec:AC-094 migração preserva pessoa legada sem inventar CPF")
    void preservesLegacyPerson() throws Exception {
        try (var db = new BaseEntitiesPostgres()) {
            db.flyway("16").migrate();
            UUID id = UUID.randomUUID();
            try (var c = db.connection(); var sql = c.prepareStatement("insert into person(id,name,email,phone_number) values (?, 'Legado', 'old@example.com', '123')")) {
                sql.setObject(1, id); sql.executeUpdate();
            }
            db.flyway(null).migrate();
            try (var c = db.connection(); var sql = c.prepareStatement("select name,email,phone_number,cpf from person where id=?")) {
                sql.setObject(1, id); try (var rows = sql.executeQuery()) {
                    assertTrue(rows.next()); assertEquals("Legado", rows.getString(1)); assertEquals("old@example.com", rows.getString(2)); assertEquals("123", rows.getString(3)); assertNull(rows.getString(4));
                }
            }
        }
    }
}
