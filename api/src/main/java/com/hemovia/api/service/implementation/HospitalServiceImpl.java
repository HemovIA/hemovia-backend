package com.hemovia.api.service.implementation;

import com.hemovia.api.model.DTOs.HospitalRequestDTO;
import com.hemovia.api.model.DTOs.HospitalResponseDTO;
import com.hemovia.api.service.HospitalService;
import org.springframework.stereotype.Service;
import com.hemovia.api.repository.HospitalRepository;
import com.hemovia.api.model.Hospital;

@Service
public class HospitalServiceImpl implements HospitalService{

    private final HospitalRepository repository;

    @Override
    public HospitalResponseDTO create(HospitalRequestDTO request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setCity(request.getCity());
        hospital.setLatitude(request.getLatitude());
        hospital.setLongitude(request.getLongitude());
        hospital.setPriority(request.getPriority());

        hospital = repository.save(hospital);
        HospitalResponseDTO response = new HospitalResponseDTO();
        response.setId(hospital.getId());
        response.setName(hospital.getName());
        response.setCity(hospital.getCity());
        response.setLatitude(hospital.getLatitude());
        response.setLongitude(hospital.getLongitude());
        response.setPriority(hospital.getPriority());
        return response;
    }

    public HospitalServiceImpl(HospitalRepository repository) {
        this.repository = repository;
    }

}
