package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.port.output.LinkedResourceChecker;
import com.hemovia.api.repository.BloodCenterRepository;
import com.hemovia.api.repository.HospitalRepository;
import com.hemovia.api.repository.VehicleRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/*
Confere nos repositórios existentes se o hospital, hemocentro ou veículo do vínculo realmente existe.
*/
@Component
public class LinkedResourceCheckerAdapter implements LinkedResourceChecker {

    private final HospitalRepository hospitals;
    private final BloodCenterRepository bloodCenters;
    private final VehicleRepository vehicles;

    public LinkedResourceCheckerAdapter(HospitalRepository hospitals, BloodCenterRepository bloodCenters,
                                        VehicleRepository vehicles) {
        this.hospitals = hospitals;
        this.bloodCenters = bloodCenters;
        this.vehicles = vehicles;
    }

    @Override
    public boolean hospitalExists(UUID id) {
        return hospitals.existsById(id);
    }

    @Override
    public boolean bloodCenterExists(UUID id) {
        return bloodCenters.existsById(id);
    }

    @Override
    public boolean vehicleExists(UUID id) {
        return vehicles.existsById(id);
    }
}
