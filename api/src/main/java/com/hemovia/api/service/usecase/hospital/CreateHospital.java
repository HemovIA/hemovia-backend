package com.hemovia.api.service.usecase.hospital;

import com.hemovia.api.dto.request.HospitalRequest;
import com.hemovia.api.dto.response.HospitalResponse;
import com.hemovia.api.model.entity.Hospital;
import com.hemovia.api.repository.jpa.HospitalRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateHospital {
    private final HospitalRepository repository;

    public CreateHospital(HospitalRepository repository) {
        this.repository = repository;
    }

    public HospitalResponse execute(HospitalRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.name());
        hospital.setCity(request.city());
        hospital.setLatitude(request.latitude());
        hospital.setLongitude(request.longitude());
        hospital.setPriority(request.priority());

        hospital = repository.save(hospital);
        return new HospitalResponse(hospital.getId(), hospital.getName(), hospital.getCity(),
                hospital.getLatitude(), hospital.getLongitude(), hospital.getPriority());
    }

}
