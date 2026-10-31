package com.hemovia.api.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MigrationSourcesTest {
    @Test @DisplayName("@spec:AC-013 conteúdo de V1 a V3 permanece inalterado")
    void ac013_existingMigrationsRemainUnchanged() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        try (var manifest = loader.getResourceAsStream("fixtures/base-entities-migration-checksums.json")) {
            assertNotNull(manifest);
            Map<String, String> checksums = new ObjectMapper().readValue(manifest, new TypeReference<>() {});
            assertEquals(3, checksums.size());
            for (var entry : checksums.entrySet()) try (var resource = loader.getResourceAsStream("db/migration/" + entry.getKey())) {
                assertNotNull(resource, entry.getKey());
                String text = new String(resource.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
                String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
                assertEquals(entry.getValue(), actual, "Não reescrever migração já existente: " + entry.getKey());
            }
        }
    }
}
