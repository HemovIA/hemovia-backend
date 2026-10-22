package com.hemovia.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemovia.api.model.DTOs.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.hemovia.api.support.BaseEntityFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

class BaseEntitiesUnitTest {
    @Test @DisplayName("@spec:AC-005 usuário público não expõe credenciais")
    void ac005_publicUserDoesNotExposeCredentials() throws Exception {
        var dto = new UserResponseDTO(UUID.randomUUID(), "Operador Sintético", "op@example.test", "00000000000", "81000000000");
        var json = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(dto));
        assertFalse(json.has("password")); assertFalse(json.has("secret"));
        assertEquals("op@example.test", json.get("email").asText());
        assertNotEquals(User.class, Person.class);
    }

    @Test @DisplayName("@spec:AC-005 veículo preserva código e capacidade em bolsas")
    void ac005_vehicleRetainsIdentity() {
        Object v = vehicle("VEICULO-TESTE"); valid(v);
        assertEquals("VEICULO-TESTE", get(v, "code")); assertEquals(20, get(v, "capacity"));
    }

    @ParameterizedTest @ValueSource(ints = {0, -1})
    @DisplayName("@spec:AC-005 veículo rejeita capacidade não positiva")
    void ac005_vehicleRejectsNonPositiveCapacity(int capacity) {
        Object v = vehicle("VEICULO-TESTE"); rejects(v, () -> set(v, "capacity", capacity));
    }

    @ParameterizedTest @ValueSource(strings = {"Hospital", "BloodCenter"})
    @DisplayName("@spec:AC-006 coordenadas zero são válidas")
    void ac006_acceptsZeroCoordinates(String kind) {
        Object o = organization(kind); valid(o);
        assertEquals(0.0, get(o, "latitude")); assertEquals(0.0, get(o, "longitude"));
    }

    @ParameterizedTest @ValueSource(strings = {"Hospital", "BloodCenter"})
    @DisplayName("@spec:AC-006 limites inclusivos de latitude e longitude são válidos")
    void ac006_acceptsCoordinateBoundaries(String kind) {
        for (double latitude : new double[]{-90, 90}) for (double longitude : new double[]{-180, 180}) {
            Object o = organization(kind); set(o, "latitude", latitude); set(o, "longitude", longitude); valid(o);
            assertEquals(latitude, get(o, "latitude")); assertEquals(longitude, get(o, "longitude"));
        }
    }

    @ParameterizedTest @ValueSource(strings = {"Hospital", "BloodCenter"})
    @DisplayName("@spec:AC-006 nome vazio e coordenadas ausentes são rejeitados")
    void ac006_rejectsBlankNameAndMissingCoordinates(String kind) {
        Object o = organization(kind); rejects(o, () -> set(o, "name", "   "));
        Object missing = organization(kind); rejects(missing, () -> set(missing, "latitude", null));
        Object missingLongitude = organization(kind); rejects(missingLongitude, () -> set(missingLongitude, "longitude", null));
    }

    @ParameterizedTest @ValueSource(doubles = {-90.001, 90.001})
    @DisplayName("@spec:AC-006 latitude fora do intervalo não é ignorada")
    void ac006_rejectsLatitude(double latitude) {
        Object h = organization("Hospital"); rejects(h, () -> set(h, "latitude", latitude));
    }

    @ParameterizedTest @ValueSource(doubles = {-180.001, 180.001})
    @DisplayName("@spec:AC-006 longitude fora do intervalo não é ignorada")
    void ac006_rejectsLongitude(double longitude) {
        Object h = organization("Hospital"); rejects(h, () -> set(h, "longitude", longitude));
    }

    @ParameterizedTest @ValueSource(ints = {0, 6})
    @DisplayName("@spec:AC-006 prioridade fora de 1 a 5 não é ignorada")
    void ac006_rejectsPriority(int priority) {
        Object h = organization("Hospital"); rejects(h, () -> set(h, "priority", priority));
    }

    @Test @DisplayName("@spec:AC-007 uma doação contém duas bolsas distintas")
    void ac007_donationContainsMultipleBags() {
        Object donor = person(), center = organization("BloodCenter"), d = donation(donor, center);
        Object first = bag(), second = bag(); set(first, "donor", donor); set(second, "donor", donor);
        attach(d, first); attach(d, second); valid(d); valid(first); valid(second);
        assertEquals(List.of(first, second), List.copyOf((Collection<?>) get(d, "bloodBags")));
        assertSame(d, get(first, "donation")); assertSame(d, get(second, "donation"));
    }

    @Test @DisplayName("@spec:AC-007 conflito de doadores é rejeitado")
    void ac007_rejectsConflictingDonors() {
        Object original = person(), other = person(); set(original, "id", UUID.randomUUID()); set(other, "id", UUID.randomUUID());
        Object d = donation(original, organization("BloodCenter")), b = bag(); set(b, "donor", other);
        rejects(b, () -> attach(d, b));
    }

    @Test @DisplayName("@spec:AC-008 bolsa mantém enums, datas civis e volume")
    void ac008_retainsCivilDatesAndTypedCharacteristics() {
        Object b = bag(); valid(b);
        assertEquals(LocalDate.of(2030, 1, 1), get(b, "collectionDate"));
        assertEquals(LocalDate.of(2030, 2, 1), get(b, "expirationDate"));
        assertEquals("POSITIVE", ((Enum<?>) get(b, "rhFactor")).name());
        assertEquals("WHOLE_BLOOD", ((Enum<?>) get(b, "component")).name());
        assertEquals(new BigDecimal("450"), get(b, "volumeMl"));
    }

    @Test @DisplayName("@spec:AC-008 validade não pode anteceder coleta")
    void ac008_rejectsExpirationBeforeCollection() {
        Object b = bag(); rejects(b, () -> set(b, "expirationDate", LocalDate.of(2029, 12, 31)));
    }

    @ParameterizedTest @ValueSource(strings = {"0", "-0.01"})
    @DisplayName("@spec:AC-008 volume informado deve ser positivo")
    void ac008_rejectsNonPositiveVolume(String volume) {
        Object b = bag(); rejects(b, () -> set(b, "volumeMl", new BigDecimal(volume)));
    }

    @Test @DisplayName("@spec:AC-009 requisição inicia pendente com quantidade em bolsas")
    void ac009_initialRequestIsPending() {
        Object r = request(organization("Hospital")); valid(r);
        assertEquals("PENDING", ((Enum<?>) get(r, "status")).name()); assertEquals(2, get(r, "quantity"));
        assertNotNull(get(r, "createdAt"));
    }

    @ParameterizedTest @ValueSource(ints = {0, -1})
    @DisplayName("@spec:AC-009 quantidade não positiva é rejeitada")
    void ac009_rejectsNonPositiveQuantity(int quantity) {
        Object r = request(organization("Hospital")); rejects(r, () -> set(r, "quantity", quantity));
    }

    @Test @DisplayName("@spec:AC-011 rota mantém sequência de pontos e unidades")
    void ac011_preservesWaypointSequence() {
        Object r = route(organization("BloodCenter"), organization("Hospital")); valid(r);
        List<?> points = List.copyOf((Collection<?>) get(r, "waypoints"));
        assertEquals(2, points.size()); assertEquals(0.0, get(points.get(0), "latitude"));
        assertEquals(-8.05, get(points.get(1), "latitude"));
        assertEquals(new BigDecimal("12.50"), get(r, "distanceKm")); assertEquals(900L, get(r, "estimatedDurationSeconds"));
    }

    @Test @DisplayName("@spec:AC-011 distância e duração negativas são rejeitadas")
    void ac011_rejectsNegativeMeasures() {
        Object r = route(organization("BloodCenter"), organization("Hospital"));
        rejects(r, () -> set(r, "distanceKm", new BigDecimal("-1")));
        Object other = route(organization("BloodCenter"), organization("Hospital"));
        rejects(other, () -> set(other, "estimatedDurationSeconds", -1L));
    }

    @Test @DisplayName("@spec:AC-012 transporte rejeita término anterior ao início")
    void ac012_rejectsReversedTransportTimes() {
        Object center = organization("BloodCenter"), hospital = organization("Hospital");
        Object t = transport(allocation(request(hospital), center), route(center, hospital), vehicle("TESTE"));
        rejects(t, () -> set(t, "completedAt", Instant.parse("2030-01-01T09:59:59Z")));
    }

    @Test @DisplayName("@spec:AC-012 leitura mantém precisão e rejeita temperatura ausente")
    void ac012_preservesTemperatureAndRejectsMissingValue() {
        Object center = organization("BloodCenter"), hospital = organization("Hospital");
        Object t = transport(allocation(request(hospital), center), route(center, hospital), vehicle("TESTE"));
        Object r = reading(t); valid(r); assertEquals(new BigDecimal("4.25"), get(r, "temperatureCelsius"));
        assertSame(t, get(r, "transport")); rejects(r, () -> set(r, "temperatureCelsius", null));
    }
}
