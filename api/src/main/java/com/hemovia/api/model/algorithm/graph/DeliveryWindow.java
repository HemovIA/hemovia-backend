package com.hemovia.api.model.algorithm.graph;

import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.exception.InvalidRouteException;
import java.time.DateTimeException;
import java.time.Instant;

public final class DeliveryWindow {
    private DeliveryWindow() { }

    public static void validate(BloodComponent component, Instant deadline, Instant departure,
                                long durationSeconds, long plateletsMaxSeconds) {
        if (component == null || departure == null || durationSeconds < 0 || plateletsMaxSeconds <= 0) {
            throw new InvalidRouteException("Dados da janela de entrega inválidos.");
        }
        if (component == BloodComponent.PLATELETS && durationSeconds > plateletsMaxSeconds) {
            throw new InvalidRouteException("A duração ultrapassa o limite de plaquetas.");
        }
        try {
            if (deadline != null && departure.plusSeconds(durationSeconds).isAfter(deadline)) {
                throw new InvalidRouteException("A chegada ultrapassa o prazo de entrega.");
            }
        } catch (DateTimeException | ArithmeticException ex) {
            throw new InvalidRouteException("A chegada estimada é inválida.");
        }
    }
}
