package com.hemovia.api.repository.adapter;

import com.hemovia.api.support.BaseEntitiesPostgres;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
class DonorUserPersistenceIT {
    @Test
    @DisplayName("@spec:AC-131 migração mantém usuários e garante vínculo opcional e único")
    void preservesUsersAndConstrainsDonorLink() throws Exception {
        UUID donor = UUID.randomUUID();
        try (var db = new BaseEntitiesPostgres()) {
            db.flyway("17").migrate();
            try (var connection = db.connection()) {
                execute(connection, "insert into person(id,name) values (?, 'Doador')", donor);
                execute(connection, """
                        insert into users(id,name,email,cpf,password,role)
                        values (?, 'Admin', 'admin@example.com', '52998224725', 'hash', 'ADMIN')
                        """, UUID.randomUUID());
            }
            db.flyway(null).migrate();
            try (var connection = db.connection(); var query = connection.createStatement()) {
                insertDonor(connection, "first@example.com", "11144477735", donor);
                insertDonor(connection, "second@example.com", "16899535009", null);
                assertThat(assertThrows(SQLException.class,
                        () -> insertDonor(connection, "other@example.com", "98765432100", donor)).getSQLState())
                        .isEqualTo("23505");
                assertThat(assertThrows(SQLException.class,
                        () -> insertDonor(connection, "missing@example.com", "98765432100", UUID.randomUUID())).getSQLState())
                        .isEqualTo("23503");
                try (var rows = query.executeQuery("select role, donor_id from users order by email")) {
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getString("role")).isEqualTo("ADMIN");
                    assertThat(rows.getObject("donor_id")).isNull();
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getString("role")).isEqualTo("DOADOR");
                    assertThat(rows.getObject("donor_id")).isEqualTo(donor);
                    assertThat(rows.next()).isTrue();
                    assertThat(rows.getObject("donor_id")).isNull();
                }
            }
        }
    }

    private void insertDonor(Connection connection, String email, String cpf, UUID donorId) throws SQLException {
        execute(connection, """
                insert into users(id,name,email,cpf,password,role,donor_id)
                values (?, 'Doador', ?, ?, 'hash', 'DOADOR', ?)
                """, UUID.randomUUID(), email, cpf, donorId);
    }

    private void execute(Connection connection, String sql, Object... arguments) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) {
                statement.setObject(i + 1, arguments[i]);
            }
            statement.executeUpdate();
        }
    }
}
