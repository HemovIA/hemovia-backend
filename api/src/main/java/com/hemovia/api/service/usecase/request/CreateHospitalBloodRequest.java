package com.hemovia.api.service.usecase.request;

import com.hemovia.api.dto.request.CreateBloodRequest;
import com.hemovia.api.dto.internal.NewBloodRequest;
import com.hemovia.api.dto.response.BloodRequestResponse;
import com.hemovia.api.repository.port.BloodRequestStore;
import com.hemovia.api.service.port.output.ClockProvider;
import com.hemovia.api.service.port.output.CurrentUserProvider;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.model.enums.RequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateHospitalBloodRequest {
    private final BloodRequestStore store;
    private final CurrentUserProvider currentUser;
    private final ClockProvider clock;

    public CreateHospitalBloodRequest(BloodRequestStore store, CurrentUserProvider currentUser, ClockProvider clock) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    public BloodRequestResponse execute(CreateBloodRequest request) {
        if (request.deliveryDeadline() == null || !request.deliveryDeadline().isAfter(clock.now())) {
            throw new InvalidDataException("O prazo de entrega deve estar no futuro.");
        }
        var user = currentUser.current();
        if (!user.worksAtHospital(user.hospitalId())) {
            throw new InvalidDataException("Usuário sem vínculo com hospital.");
        }
        var pending = new NewBloodRequest(user.hospitalId(), request.bloodType(), request.rhFactor(),
                request.component(), request.quantity(), request.priority(), request.deliveryDeadline());
        return new BloodRequestResponse(store.save(pending), RequestStatus.PENDING);
    }
}
