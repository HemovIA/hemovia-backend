package com.hemovia.api.support;

import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;

import java.sql.*;
import java.util.List;
import java.util.UUID;

/** Um schema próprio por teste; não usa application.yaml, .env ou banco padrão. */
public final class BaseEntitiesPostgres implements AutoCloseable {
    public final String schema = "acceptance_" + UUID.randomUUID().toString().replace("-", "");
    private final String url = required("HEMOVIA_TEST_JDBC_URL");
    private final String user = required("HEMOVIA_TEST_DB_USER");
    private final String password = System.getenv().getOrDefault("HEMOVIA_TEST_DB_PASSWORD", "");
    private EntityManagerFactory factory;

    public BaseEntitiesPostgres() throws SQLException {
        if (!url.startsWith("jdbc:postgresql:")) throw new AssertionError("Integração exige PostgreSQL real");
        try (Connection c = DriverManager.getConnection(url, user, password); Statement s = c.createStatement()) {
            if (!c.getMetaData().getDatabaseProductName().equals("PostgreSQL")) throw new AssertionError("Banco não é PostgreSQL");
            s.execute("CREATE SCHEMA " + schema);
        }
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new AssertionError("Integração não executada: informe " + name + " para um PostgreSQL dedicado de teste");
        return value;
    }

    public Connection connection() throws SQLException {
        Connection c = DriverManager.getConnection(url, user, password); c.setSchema(schema); return c;
    }

    public Flyway flyway(String target) {
        var config = Flyway.configure().dataSource(url, user, password).schemas(schema).defaultSchema(schema)
                .locations("classpath:db/migration").cleanDisabled(true);
        if (target != null) config.target(target);
        return config.load();
    }

    public EntityManagerFactory current() {
        flyway(null).migrate();
        var configuration = new org.hibernate.cfg.Configuration();
        for (String name : List.of("Person", "Hospital", "BloodCenter", "Vehicle", "Donation", "BloodBag",
                "BloodRequest", "Allocation", "AllocationItem", "DeliveryRoute", "RouteWaypoint", "Transport", "TelemetryReading", "StockMovement")) {
            configuration.addAnnotatedClass(BaseEntityFixtures.type(name));
        }
        // O usuário agora é uma entidade da camada de infraestrutura, mas o StockMovement ainda aponta para ela
        configuration.addAnnotatedClass(com.hemovia.api.infrastructure.persistence.entity.UserEntity.class);
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", user);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.default_schema", schema);
        // Match Spring Boot's production naming convention in this standalone Hibernate bootstrap.
        configuration.setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
        configuration.setProperty("hibernate.show_sql", "false");
        factory = configuration.buildSessionFactory();
        return factory;
    }

    public long count(String sql) throws SQLException {
        try (Connection c = connection(); Statement s = c.createStatement(); ResultSet rows = s.executeQuery(sql)) {
            rows.next(); return rows.getLong(1);
        }
    }

    @Override public void close() throws SQLException {
        if (factory != null) factory.close();
        // O identificador é gerado internamente, nunca vem de variável de ambiente.
        try (Connection c = connection(); Statement s = c.createStatement()) { s.execute("DROP SCHEMA " + schema + " CASCADE"); }
    }
}
