package com.hemovia.api.repository.adapter;

import com.hemovia.api.model.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class DonorRegistrationStore {
    private final JdbcClient jdbc;

    public DonorRegistrationStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean issue(String email, String hash, Instant now, Instant expires, Instant eligibleSince) {
        return jdbc.sql("""
                INSERT INTO donor_registration_tokens (email, token_hash, issued_at, expires_at)
                VALUES (:email, :hash, :now, :expires)
                ON CONFLICT (email) DO UPDATE
                SET token_hash = EXCLUDED.token_hash, issued_at = EXCLUDED.issued_at,
                    expires_at = EXCLUDED.expires_at, consumed = FALSE
                WHERE donor_registration_tokens.issued_at <= :eligibleSince
                """)
                .param("email", email)
                .param("hash", hash)
                .param("now", utc(now))
                .param("expires", utc(expires))
                .param("eligibleSince", utc(eligibleSince))
                .update() == 1;
    }

    public Optional<String> lockValidEmail(String hash, Instant now) {
        return jdbc.sql("""
                SELECT email FROM donor_registration_tokens
                WHERE token_hash = :hash AND NOT consumed AND expires_at > :now
                FOR UPDATE
                """)
                .param("hash", hash)
                .param("now", utc(now))
                .query(String.class)
                .optional();
    }

    public void consume(String hash) {
        jdbc.sql("UPDATE donor_registration_tokens SET consumed = TRUE WHERE token_hash = :hash")
                .param("hash", hash)
                .update();
    }

    public boolean insertDonor(User user) {
        return jdbc.sql("""
                INSERT INTO users (id, name, email, cpf, password, role, active, donor_id)
                VALUES (:id, :name, :email, :cpf, :passwordHash, :role, :active, :donorId)
                ON CONFLICT DO NOTHING
                """)
                .param("id", user.id())
                .param("name", user.name())
                .param("email", user.email().value())
                .param("cpf", user.cpf().digits())
                .param("passwordHash", user.passwordHash())
                .param("role", user.role().name())
                .param("active", user.isActive())
                .param("donorId", user.donorId())
                .update() == 1;
    }

    public void deleteExpired(Instant now) {
        jdbc.sql("DELETE FROM donor_registration_tokens WHERE expires_at <= :now")
                .param("now", utc(now))
                .update();
    }

    private OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
