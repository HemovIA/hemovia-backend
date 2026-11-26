package com.hemovia.api.service.usecase.vehicle;

import com.hemovia.api.dto.request.VehicleRequest;
import com.hemovia.api.dto.response.VehicleResponse;
import com.hemovia.api.model.entity.Vehicle;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.repository.jpa.VehicleRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManageVehiclesTest {
    private final VehicleRepository repository = mock(VehicleRepository.class);
    private final ManageVehicles service = new ManageVehicles(repository);

    @Test
    @DisplayName("@spec:AC-065 cadastro apara código e retorna os dados persistidos")
    void createTrimsCodeAndReturnsSavedVehicle() {
        UUID id = UUID.randomUUID();
        when(repository.saveAndFlush(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(id);
            return vehicle;
        });

        VehicleResponse result = service.create(new VehicleRequest("  V-01  ", 20));

        verify(repository).existsByCode("V-01");
        assertEquals(new VehicleResponse(id, "V-01", 20), result);
    }

    @Test
    void rejectsDuplicateAfterTrimmingWithoutSaving() {
        when(repository.existsByCode("V-01")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> service.create(new VehicleRequest(" V-01 ", 20)));
        verify(repository, never()).saveAndFlush(any(Vehicle.class));
    }

    @Test
    void translatesConcurrentDuplicateCode() {
        when(repository.saveAndFlush(any(Vehicle.class)))
                .thenThrow(integrityViolation("uk_vehicles_code"));

        assertThrows(DuplicateResourceException.class,
                () -> service.create(new VehicleRequest("V-01", 20)));
    }

    @Test
    void preservesOtherIntegrityFailures() {
        DataIntegrityViolationException failure = integrityViolation("ck_vehicles_capacity_positive");
        when(repository.saveAndFlush(any(Vehicle.class))).thenThrow(failure);

        assertSame(failure, assertThrows(DataIntegrityViolationException.class,
                () -> service.create(new VehicleRequest("V-01", 20))));
    }

    private static DataIntegrityViolationException integrityViolation(String constraint) {
        return new DataIntegrityViolationException("constraint failure",
                new ConstraintViolationException("constraint failure", new SQLException(), constraint));
    }

    @Test
    void listsVehiclesUsingCodeThenIdOrdering() {
        Vehicle first = new Vehicle();
        first.setId(UUID.randomUUID());
        first.setCode("A");
        first.setCapacity(5);
        Vehicle second = new Vehicle();
        second.setId(UUID.randomUUID());
        second.setCode("B");
        second.setCapacity(10);
        Sort sort = Sort.by("code", "id");
        when(repository.findAll(sort)).thenReturn(List.of(first, second));

        assertEquals(List.of(new VehicleResponse(first.getId(), "A", 5),
                new VehicleResponse(second.getId(), "B", 10)), service.list());
        verify(repository).findAll(sort);
    }
}
