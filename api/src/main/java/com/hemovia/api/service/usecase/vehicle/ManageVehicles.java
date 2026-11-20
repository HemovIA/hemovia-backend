package com.hemovia.api.service.usecase.vehicle;

import com.hemovia.api.dto.request.VehicleRequest;
import com.hemovia.api.dto.response.VehicleResponse;
import com.hemovia.api.model.entity.Vehicle;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.repository.jpa.VehicleRepository;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ManageVehicles {
    private final VehicleRepository vehicles;

    public ManageVehicles(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    public VehicleResponse create(VehicleRequest request) {
        String code = request.code().trim();
        if (vehicles.existsByCode(code)) {
            throw new DuplicateResourceException("Código de veículo já cadastrado");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setCode(code);
        vehicle.setCapacity(request.capacity());
        try {
            return response(vehicles.saveAndFlush(vehicle));
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateCode(exception)) {
                throw new DuplicateResourceException("Código de veículo já cadastrado");
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> list() {
        return vehicles.findAll(Sort.by("code", "id")).stream()
                .map(ManageVehicles::response)
                .toList();
    }

    private static boolean isDuplicateCode(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && "uk_vehicles_code".equals(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    private static VehicleResponse response(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getCode(), vehicle.getCapacity());
    }
}
