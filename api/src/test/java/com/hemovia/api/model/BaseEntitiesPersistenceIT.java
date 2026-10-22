package com.hemovia.api.model;

import com.hemovia.api.support.BaseEntitiesPostgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static com.hemovia.api.support.BaseEntityFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

/** Executar explicitamente: mvn -Dtest=BaseEntitiesPersistenceIT test. Sem skips. */
class BaseEntitiesPersistenceIT {
    private BaseEntitiesPostgres db;
    @BeforeEach void createIsolatedSchema() throws Exception { db = new BaseEntitiesPostgres(); }
    @AfterEach void removeOnlyOwnedSchema() throws Exception { if (db != null) db.close(); }

    private record Data(Object donor, Object center, Object hospital, Object vehicle, Object donation,
                        Object bag, Object request, Object allocation, Object route) {}

    private Data seed(EntityManager em) {
        Object p = person(), c = organization("BloodCenter"), h = organization("Hospital"), v = vehicle("TESTE-" + UUID.randomUUID());
        em.persist(p); em.persist(c); em.persist(h); em.persist(v);
        Object d = donation(p, c); em.persist(d);
        Object b = bag(); set(b, "donor", p); set(b, "bloodCenter", c); attach(d, b); em.persist(b);
        Object r = request(h); em.persist(r); Object a = allocation(r, c); em.persist(a);
        Object route = route(c, h); em.persist(route); em.flush();
        return new Data(p, c, h, v, d, b, r, a, route);
    }

    @Test @DisplayName("@spec:AC-005 PostgreSQL rejeita veículo duplicado e permite repetir doador")
    void ac005_vehicleUniquenessAndReusableDonor() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em);
            Object second = bag(); set(second, "donor", d.donor()); attach(d.donation(), second); em.persist(second);
            em.getTransaction().commit();
            assertEquals(2, db.count("SELECT count(*) FROM blood_bags"));
            em.getTransaction().begin(); em.persist(vehicle((String) get(d.vehicle(), "code")));
            assertThrows(PersistenceException.class, em::flush); em.getTransaction().rollback();
            assertEquals(1, db.count("SELECT count(*) FROM vehicles"));
        }
    }

    @Test @DisplayName("@spec:AC-006 PostgreSQL também impede coordenadas fora do intervalo")
    void ac006_databaseRejectsInvalidCoordinates() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) { em.getTransaction().begin(); seed(em); em.getTransaction().commit(); }
        for (String table : List.of("hospital", "blood_centers")) try (var c = db.connection(); var s = c.createStatement()) {
            assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE " + table + " SET latitude=91")).getSQLState());
            assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE " + table + " SET longitude=-181")).getSQLState());
            assertEquals("23502", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE " + table + " SET latitude=NULL")).getSQLState());
        }
    }

    @Test @DisplayName("@spec:AC-007 persistir duas bolsas e desfazer vínculo preserva registros")
    void ac007_multipleBagsAndNonDestructiveUnlink() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em); Object second = bag();
            set(second, "donor", d.donor()); attach(d.donation(), second); em.persist(second); em.getTransaction().commit();
            Object donationId = get(d.donation(), "id"), bagId = get(second, "id"); em.clear();
            Object loaded = em.find(type("Donation"), donationId);
            assertEquals(2, ((Collection<?>) get(loaded, "bloodBags")).size());
            em.getTransaction().begin(); Object loadedBag = em.find(type("BloodBag"), bagId);
            ((Collection<?>) get(loaded, "bloodBags")).remove(loadedBag); set(loadedBag, "donation", null);
            em.getTransaction().commit(); em.clear();
            assertNotNull(em.find(type("BloodBag"), bagId));
            assertEquals(1, db.count("SELECT count(*) FROM person")); assertEquals(2, db.count("SELECT count(*) FROM blood_bags"));
        }
    }

    @Test @DisplayName("@spec:AC-008 banco rejeita códigos, datas e volume inválidos")
    void ac008_databaseRejectsInvalidBagValues() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) { em.getTransaction().begin(); seed(em); em.getTransaction().commit(); }
        for (String mutation : List.of("rh_factor='UNKNOWN'", "component='UNKNOWN'", "blood_type_enum='UNKNOWN'",
                "expiration_date=collection_date - 1", "volume_ml=0")) {
            try (var c = db.connection(); var s = c.createStatement()) {
                SQLException error = assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE blood_bags SET " + mutation));
                assertEquals("23514", error.getSQLState(), "CHECK deve impedir dados inválidos mesmo fora do ORM");
            }
        }
    }

    @Test @DisplayName("@spec:AC-009 requisição preserva unidades e rejeita hospital inexistente")
    void ac009_requestRoundTripAndForeignKey() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em); em.getTransaction().commit(); em.clear();
            Object loaded = em.find(type("BloodRequest"), get(d.request(), "id"));
            assertEquals(2, get(loaded, "quantity")); assertEquals("PENDING", ((Enum<?>) get(loaded, "status")).name());
            assertNotNull(get(loaded, "createdAt"));
        }
        try (var c = db.connection(); var s = c.createStatement()) {
            for (String mutation : List.of("quantity=0", "rh_factor='UNKNOWN'", "blood_type='UNKNOWN'", "component='UNKNOWN'"))
                assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE blood_requests SET " + mutation)).getSQLState());
            assertEquals("23503", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE blood_requests SET hospital_id='" + UUID.randomUUID() + "'")).getSQLState());
        }
    }

    private String reserve(UUID allocationId, UUID bagId, CountDownLatch start) throws Exception {
        try (var c = db.connection()) {
            c.setAutoCommit(false); start.await(10, TimeUnit.SECONDS);
            try (var s = c.prepareStatement("INSERT INTO allocation_items(id,allocation_id,blood_bag_id,status,created_at) VALUES (?,?,?,'RESERVED',CURRENT_TIMESTAMP)")) {
                s.setObject(1, UUID.randomUUID()); s.setObject(2, allocationId); s.setObject(3, bagId);
                s.executeUpdate(); c.commit(); return "committed";
            } catch (SQLException e) { c.rollback(); return e.getSQLState(); }
        }
    }

    @Test @DisplayName("@spec:AC-010 reservas concorrentes têm um vencedor e preservam liberação")
    void ac010_concurrentReservationAndReleaseHistory() throws Exception {
        var factory = db.current(); UUID bagId, firstAllocation, secondAllocation;
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em); Object other = allocation(d.request(), d.center()); em.persist(other);
            em.getTransaction().commit(); bagId = (UUID) get(d.bag(), "id");
            firstAllocation = (UUID) get(d.allocation(), "id"); secondAllocation = (UUID) get(other, "id");
        }
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<String> first = executor.submit(() -> reserve(firstAllocation, bagId, start));
            Future<String> second = executor.submit(() -> reserve(secondAllocation, bagId, start)); start.countDown();
            assertEquals(Set.of("committed", "23505"), new HashSet<>(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS))));
        }
        assertEquals(1, db.count("SELECT count(*) FROM allocation_items WHERE status='RESERVED'"));
        UUID oldAllocation;
        try (var c = db.connection(); var s = c.createStatement(); var r = s.executeQuery("SELECT allocation_id FROM allocation_items")) { r.next(); oldAllocation = r.getObject(1, UUID.class); }
        UUID next = oldAllocation.equals(firstAllocation) ? secondAllocation : firstAllocation;
        try (var c = db.connection(); var s = c.createStatement()) { s.executeUpdate("UPDATE allocation_items SET status='IN_TRANSIT'"); }
        assertEquals("23505", reserve(next, bagId, new CountDownLatch(0)), "Em trânsito também precisa impedir reserva duplicada");
        try (var c = db.connection(); var s = c.createStatement()) { s.executeUpdate("UPDATE allocation_items SET status='RELEASED', released_at=CURRENT_TIMESTAMP"); }
        assertEquals("committed", reserve(next, bagId, new CountDownLatch(0)));
        assertEquals(2, db.count("SELECT count(*) FROM allocation_items"));
        assertEquals(1, db.count("SELECT count(*) FROM allocation_items WHERE status='RELEASED'"));
    }

    @Test @DisplayName("@spec:AC-010 atualização de versão obsoleta é rejeitada")
    void ac010_optimisticLockRejectsLostUpdate() {
        var factory = db.current(); Object id;
        try (var em = factory.createEntityManager()) { em.getTransaction().begin(); Data d = seed(em); em.getTransaction().commit(); id = get(d.bag(), "id"); }
        try (var first = factory.createEntityManager(); var second = factory.createEntityManager()) {
            first.getTransaction().begin(); second.getTransaction().begin();
            Object a = first.find(type("BloodBag"), id), b = second.find(type("BloodBag"), id);
            set(a, "status", code("BloodBagStatus", "RESERVED")); first.getTransaction().commit();
            set(b, "status", code("BloodBagStatus", "DISCARDED"));
            var failure = assertThrows(PersistenceException.class, () -> second.getTransaction().commit());
            Throwable cause = failure; while (!(cause instanceof jakarta.persistence.OptimisticLockException) && cause.getCause() != null) cause = cause.getCause();
            assertInstanceOf(jakarta.persistence.OptimisticLockException.class, cause);
            if (second.getTransaction().isActive()) second.getTransaction().rollback();
        }
    }

    @Test @DisplayName("@spec:AC-011 ordem de pontos sobrevive ao banco e posição duplicada falha")
    void ac011_routeOrderAndUniquePosition() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em); em.getTransaction().commit(); em.clear();
            Object route = em.find(type("DeliveryRoute"), get(d.route(), "id"));
            List<?> points = List.copyOf((Collection<?>) get(route, "waypoints"));
            assertEquals(0.0, get(points.get(0), "latitude")); assertEquals(-8.05, get(points.get(1), "latitude"));
        }
        try (var c = db.connection(); var s = c.createStatement()) {
            assertEquals("23505", assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO route_waypoints(route_id,position,latitude,longitude) SELECT route_id,position,latitude,longitude FROM route_waypoints LIMIT 1")).getSQLState());
            assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE delivery_routes SET distance_km=-1")).getSQLState());
        }
    }

    @Test @DisplayName("@spec:AC-012 transporte, leituras e histórico sobrevivem sem cascata destrutiva")
    void ac012_transportTelemetryAndHistoryRoundTrip() throws Exception {
        var factory = db.current();
        try (var em = factory.createEntityManager()) {
            em.getTransaction().begin(); Data d = seed(em); em.persist(item(d.allocation(), d.bag()));
            Object t = transport(d.allocation(), d.route(), d.vehicle()); em.persist(t); Object r = reading(t); em.persist(r);
            Object movement = entity("StockMovement"); set(movement, "bloodBag", d.bag());
            set(movement, "previousStatus", code("BloodBagStatus", "AVAILABLE")); set(movement, "newStatus", code("BloodBagStatus", "RESERVED"));
            set(movement, "movementType", code("MovementType", "RESERVATION")); set(movement, "occurredAt", Instant.parse("2030-01-01T10:00:00Z")); em.persist(movement);
            em.getTransaction().commit(); em.clear();
            Object loaded = em.find(type("TelemetryReading"), get(r, "id"));
            assertEquals(0, new BigDecimal("4.25").compareTo((BigDecimal) get(loaded, "temperatureCelsius")));
            assertEquals(get(t, "id"), get(get(loaded, "transport"), "id"));
            em.getTransaction().begin(); em.remove(em.find(type("Person"), get(d.donor(), "id")));
            assertThrows(PersistenceException.class, em::flush); em.getTransaction().rollback();
            assertEquals(1, db.count("SELECT count(*) FROM stock_movements")); assertEquals(1, db.count("SELECT count(*) FROM blood_bags"));
        }
        try (var c = db.connection(); var s = c.createStatement()) {
            assertEquals("23502", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE telemetry_readings SET temperature_celsius=NULL")).getSQLState());
            for (String value : List.of("NaN", "Infinity", "-Infinity"))
                assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE telemetry_readings SET temperature_celsius='" + value + "'::numeric")).getSQLState());
            assertEquals("23514", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE transports SET completed_at=started_at-INTERVAL '1 second'")).getSQLState());
            assertEquals("23503", assertThrows(SQLException.class, () -> s.executeUpdate("UPDATE transports SET vehicle_id='" + UUID.randomUUID() + "'")).getSQLState());
        }
    }

    @Test @DisplayName("@spec:AC-013 banco vazio recebe esquema completo e migrate é repetível")
    void ac013_emptyDatabaseAndRepeatedMigrate() throws Exception {
        var result = db.flyway(null).migrate(); assertTrue(result.migrationsExecuted >= 7, "V4 a V7 ainda precisam ser implementadas");
        assertEquals(0, db.flyway(null).migrate().migrationsExecuted);
        for (String table : List.of("blood_centers", "vehicles", "blood_requests", "allocations", "allocation_items", "delivery_routes", "transports", "telemetry_readings", "stock_movements")) {
            assertEquals(0, db.count("SELECT count(*) FROM " + table));
        }
        assertNotNull(db.current()); // Hibernate validate, sem criar esquema automaticamente.
    }

    private static final UUID DONOR = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BAG = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DONATION = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private void legacy(String rh) throws Exception {
        db.flyway("3").migrate();
        try (var c = db.connection(); var s = c.createStatement()) {
            s.executeUpdate("INSERT INTO person VALUES ('" + DONOR + "','Pessoa Sintética','donor@example.test','81000000000')");
            s.executeUpdate("INSERT INTO blood_bags(id,rh_factor,component,collection_date,expiration_date,status,blood_type_enum,donor_id) VALUES ('" + BAG + "','" + rh + "','WHOLE_BLOOD','2030-01-01','2030-02-01','AVAILABLE','O','" + DONOR + "')");
            s.executeUpdate("INSERT INTO donations(id,collection_date,collection_center,blood_bag_id) VALUES ('" + DONATION + "','2030-01-01','Centro Sintético','" + BAG + "')");
        }
    }

    @Test @DisplayName("@spec:AC-013 upgrade preserva IDs e vínculo legado sem fabricar origem")
    void ac013_upgradePreservesLegacyLinks() throws Exception {
        legacy("POSITIVE"); db.flyway(null).migrate();
        try (var c = db.connection(); var s = c.createStatement(); var r = s.executeQuery("SELECT id,donation_id,donor_id FROM blood_bags")) {
            assertTrue(r.next()); assertEquals(BAG, r.getObject("id", UUID.class)); assertEquals(DONATION, r.getObject("donation_id", UUID.class)); assertEquals(DONOR, r.getObject("donor_id", UUID.class));
            assertFalse(r.next());
        }
        assertEquals(1, db.count("SELECT count(*) FROM person")); assertEquals(0, db.count("SELECT count(*) FROM blood_centers"));
    }

    @Test @DisplayName("@spec:AC-013 código legado inválido aborta migração sem perda")
    void ac013_invalidLegacyDataRollsBack() throws Exception {
        legacy("UNKNOWN"); assertThrows(org.flywaydb.core.api.FlywayException.class, () -> db.flyway(null).migrate());
        assertEquals(1, db.count("SELECT count(*) FROM blood_bags WHERE rh_factor='UNKNOWN'"));
        assertEquals(1, db.count("SELECT count(*) FROM donations"));
    }

    @Test @DisplayName("@spec:AC-013 bolsa vinculada a duas doações legadas não é escolhida arbitrariamente")
    void ac013_ambiguousLegacyDonationAbortsMigration() throws Exception {
        legacy("POSITIVE");
        try (var c = db.connection(); var s = c.createStatement()) {
            s.executeUpdate("INSERT INTO donations(id,collection_date,collection_center,blood_bag_id) VALUES ('00000000-0000-0000-0000-000000000004','2030-01-01','Centro Sintético','" + BAG + "')");
        }
        assertThrows(org.flywaydb.core.api.FlywayException.class, () -> db.flyway(null).migrate());
        assertEquals(2, db.count("SELECT count(*) FROM donations WHERE blood_bag_id='" + BAG + "'"));
        assertEquals(1, db.count("SELECT count(*) FROM blood_bags"));
    }

    @Test @DisplayName("@spec:AC-014 integração realmente consulta PostgreSQL isolado")
    void ac014_realPostgresIsUsed() throws Exception {
        try (var c = db.connection()) { assertEquals("PostgreSQL", c.getMetaData().getDatabaseProductName()); assertEquals(db.schema, c.getSchema()); }
    }
}
