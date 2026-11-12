package com.hemovia.api.repository.adapter;

import com.hemovia.api.dto.request.CreateBloodRequest;
import com.hemovia.api.repository.port.BloodRequestStore;
import com.hemovia.api.model.entity.BloodRequest;
import com.hemovia.api.repository.jpa.BloodRequestRepository;
import com.hemovia.api.repository.jpa.HospitalRepository;
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
