package com.hemovia.api.infrastructure.persistence.adapter;

import com.hemovia.api.application.dto.request.CreateBloodRequest;
import com.hemovia.api.application.port.output.BloodRequestStore;
import com.hemovia.api.model.BloodRequest;
import com.hemovia.api.repository.BloodRequestRepository;
import com.hemovia.api.repository.HospitalRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class BloodRequestStoreAdapter implements BloodRequestStore {
    private final BloodRequestRepository requests;
    private final HospitalRepository hospitals;

    public BloodRequestStoreAdapter(BloodRequestRepository requests, HospitalRepository hospitals) {
        this.requests = requests;
        this.hospitals = hospitals;
    }

    @Override
    public UUID save(UUID hospitalId, CreateBloodRequest input) {
        BloodRequest request = new BloodRequest();
        request.setHospital(hospitals.getReferenceById(hospitalId));
        request.setBloodType(input.bloodType());
        request.setRhFactor(input.rhFactor());
        request.setComponent(input.component());
        request.setQuantity(input.quantity());
        request.setPriority(input.priority());
        request.setDeliveryDeadline(input.deliveryDeadline());
        return requests.saveAndFlush(request).getId();
    }
}
