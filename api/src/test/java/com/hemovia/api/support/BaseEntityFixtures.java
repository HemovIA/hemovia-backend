package com.hemovia.api.support;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/** Fixtures de contrato: não implementam nem simulam as entidades ausentes. */
public final class BaseEntityFixtures {
    private BaseEntityFixtures() {}

    public static Class<?> type(String name) {
        try {
            return Class.forName("com.hemovia.api.model." + name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Entidade/tipo ainda não implementado: " + name, e);
        }
    }

    public static Object entity(String name) {
        try {
            var constructor = type(name).getDeclaredConstructor();
            if (!constructor.trySetAccessible()) throw new AssertionError("Construtor JPA inacessível: " + name);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("É necessário construtor JPA sem argumentos: " + name, e);
        }
    }

    public static Object call(Object target, String method, Object... args) {
        Method selected = null;
        for (Method candidate : target.getClass().getMethods()) {
            if (candidate.getName().equals(method) && candidate.getParameterCount() == args.length) {
                selected = candidate;
                break;
            }
        }
        if (selected == null) throw new AssertionError("Contrato ausente: " + target.getClass().getSimpleName() + "." + method);
        try {
            return selected.invoke(target, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            if (e.getCause() instanceof Error error) throw error;
            throw new AssertionError(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Não foi possível exercer " + method, e);
        } catch (IllegalArgumentException e) {
            throw new AssertionError("Assinatura incompatível com o contrato testado: " + method, e);
        }
    }

    public static Object set(Object bean, String property, Object value) {
        call(bean, "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1), value);
        return bean;
    }

    public static Object get(Object bean, String property) {
        return call(bean, "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object code(String name, String value) {
        return Enum.valueOf((Class<? extends Enum>) type("enums." + name).asSubclass(Enum.class), value);
    }

    public static void valid(Object bean) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(bean);
            assertTrue(violations.isEmpty(), () -> "Registro válido rejeitado: " + violations);
        }
    }

    public static void rejects(Object bean, Runnable mutation) {
        // Apenas uma rejeição do domínio/Bean Validation conta. Falta de classe,
        // método ou erro da fixture é AssertionError e não pode aprovar o teste.
        try {
            mutation.run();
        } catch (IllegalArgumentException | jakarta.validation.ConstraintViolationException expected) {
            return;
        }
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(bean).isEmpty(), "Entrada inválida aceita silenciosamente");
        }
    }

    public static Object person() {
        Object p = entity("Person");
        set(p, "name", "Doador Sintético"); set(p, "email", "doador@example.test");
        return set(p, "phoneNumber", "81000000000");
    }

    public static Object organization(String name) {
        Object o = entity(name);
        set(o, "name", "Unidade Sintética"); set(o, "city", "Recife");
        set(o, "latitude", 0.0); set(o, "longitude", 0.0);
        if (name.equals("Hospital")) set(o, "priority", 1);
        return o;
    }

    public static Object vehicle(String identifier) {
        Object v = entity("Vehicle"); set(v, "code", identifier);
        return set(v, "capacity", 20);
    }

    public static Object bag() {
        Object b = entity("BloodBag");
        set(b, "bloodTypeEnum", code("BloodType", "O"));
        set(b, "rhFactor", code("RhFactor", "POSITIVE"));
        set(b, "component", code("BloodComponent", "WHOLE_BLOOD"));
        set(b, "collectionDate", LocalDate.of(2030, 1, 1));
        set(b, "expirationDate", LocalDate.of(2030, 2, 1));
        set(b, "status", code("BloodBagStatus", "AVAILABLE"));
        return set(b, "volumeMl", new BigDecimal("450"));
    }

    public static Object donation(Object donor, Object center) {
        Object d = entity("Donation"); set(d, "collectionDate", LocalDate.of(2030, 1, 1));
        set(d, "collectionCenter", "Centro Sintético"); set(d, "donor", donor);
        return set(d, "bloodCenter", center);
    }

    @SuppressWarnings("unchecked")
    public static void attach(Object donation, Object bag) {
        set(bag, "donation", donation);
        ((Collection<Object>) get(donation, "bloodBags")).add(bag);
    }

    public static Object request(Object hospital) {
        Object r = entity("BloodRequest"); set(r, "hospital", hospital);
        set(r, "bloodType", code("BloodType", "O")); set(r, "rhFactor", code("RhFactor", "POSITIVE"));
        set(r, "component", code("BloodComponent", "WHOLE_BLOOD"));
        return set(r, "quantity", 2);
    }

    public static Object allocation(Object request, Object center) {
        Object a = entity("Allocation"); set(a, "request", request); set(a, "bloodCenter", center);
        return set(a, "status", code("AllocationStatus", "ALLOCATED"));
    }

    public static Object item(Object allocation, Object bag) {
        Object i = entity("AllocationItem"); set(i, "allocation", allocation); set(i, "bloodBag", bag);
        set(i, "createdAt", Instant.parse("2030-01-01T10:00:00Z"));
        return set(i, "status", code("ReservationStatus", "RESERVED"));
    }

    public static Object waypoint(double latitude, double longitude) {
        Object p = entity("RouteWaypoint"); set(p, "latitude", latitude); return set(p, "longitude", longitude);
    }

    @SuppressWarnings("unchecked")
    public static Object route(Object center, Object hospital) {
        Object r = entity("DeliveryRoute"); set(r, "origin", center); set(r, "destination", hospital);
        set(r, "distanceKm", new BigDecimal("12.50")); set(r, "estimatedDurationSeconds", 900L);
        ((Collection<Object>) get(r, "waypoints")).add(waypoint(0, 0));
        ((Collection<Object>) get(r, "waypoints")).add(waypoint(-8.05, -34.9));
        return r;
    }

    public static Object transport(Object allocation, Object route, Object vehicle) {
        Object t = entity("Transport"); set(t, "allocation", allocation); set(t, "route", route);
        set(t, "vehicle", vehicle); set(t, "status", code("TransportStatus", "IN_TRANSIT"));
        return set(t, "startedAt", Instant.parse("2030-01-01T10:00:00Z"));
    }

    public static Object reading(Object transport) {
        Object r = entity("TelemetryReading"); set(r, "transport", transport); set(r, "sensorId", "sensor-sintetico");
        set(r, "measuredAt", Instant.parse("2030-01-01T10:00:01Z"));
        set(r, "receivedAt", Instant.parse("2030-01-01T10:00:02Z"));
        return set(r, "temperatureCelsius", new BigDecimal("4.25"));
    }
}
