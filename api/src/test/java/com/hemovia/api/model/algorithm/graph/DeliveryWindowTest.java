package com.hemovia.api.model.algorithm.graph;

import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.exception.InvalidRouteException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryWindowTest {
    private static final Instant DEPARTURE = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    @DisplayName("@spec:AC-049 rejeita 85 minutos de plaquetas para limite configurado de 60")
    void enforcesConfiguredPlateletsLimitIncludingBoundary() {
        assertThrows(InvalidRouteException.class, () -> DeliveryWindow.validate(BloodComponent.PLATELETS, null, DEPARTURE, 85 * 60, 60 * 60));
        assertDoesNotThrow(() -> DeliveryWindow.validate(BloodComponent.PLATELETS, null, DEPARTURE, 60 * 60, 60 * 60));
        assertDoesNotThrow(() -> DeliveryWindow.validate(BloodComponent.PLATELETS, null, DEPARTURE, 85 * 60, 90 * 60));
    }

    @Test
    @DisplayName("@spec:AC-049 aplica prazo a todos os componentes e aceita igualdade e legado sem prazo")
    void enforcesDeadlineForEveryComponentAndAllowsLegacy() {
        for (BloodComponent component : BloodComponent.values()) {
            assertDoesNotThrow(() -> DeliveryWindow.validate(component, DEPARTURE.plusSeconds(60), DEPARTURE, 60, 3600));
            assertThrows(InvalidRouteException.class, () -> DeliveryWindow.validate(component, DEPARTURE.plusSeconds(59), DEPARTURE, 60, 3600));
            assertDoesNotThrow(() -> DeliveryWindow.validate(component, null, DEPARTURE, 60, 3600));
        }
        assertThrows(InvalidRouteException.class, () -> DeliveryWindow.validate(BloodComponent.PLASMA, Instant.MAX, Instant.MAX, 1, 3600));
    }
}
