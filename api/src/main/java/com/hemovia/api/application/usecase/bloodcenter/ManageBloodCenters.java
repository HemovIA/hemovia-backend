package com.hemovia.api.application.usecase.bloodcenter;

import com.hemovia.api.application.dto.request.BloodCenterRequest;
import com.hemovia.api.application.dto.response.BloodCenterResponse;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.model.BloodCenter;
import com.hemovia.api.repository.BloodCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ManageBloodCenters {
    private final BloodCenterRepository centers;
    private final ClockProvider clock;

    public ManageBloodCenters(BloodCenterRepository centers, ClockProvider clock) {
        this.centers = centers;
        this.clock = clock;
    }

    public BloodCenterResponse create(BloodCenterRequest request) {
        var center = new BloodCenter();
        apply(center, request);
        return response(centers.save(center));
    }

    @Transactional(readOnly = true)
    public List<BloodCenterResponse> list() {
        return centers.findAllByDeletedAtIsNullOrderByNameAscIdAsc().stream()
                .map(ManageBloodCenters::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public BloodCenterResponse get(UUID id) {
        return response(centers.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hemocentro", id)));
    }

    public BloodCenterResponse update(UUID id, BloodCenterRequest request) {
        var center = lockActiveCenter(id);
        apply(center, request);
        return response(center);
    }

    public void delete(UUID id) {
        lockActiveCenter(id).markDeleted(clock.now());
    }

    private BloodCenter lockActiveCenter(UUID id) {
        return centers.findActiveForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hemocentro", id));
    }

    private static void apply(BloodCenter center, BloodCenterRequest request) {
        center.setName(request.name());
        center.setCity(request.city());
        center.setLatitude(request.latitude());
        center.setLongitude(request.longitude());
    }

    private static BloodCenterResponse response(BloodCenter center) {
        return new BloodCenterResponse(center.getId(), center.getName(), center.getCity(),
                center.getLatitude(), center.getLongitude());
    }
}
