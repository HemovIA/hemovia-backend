package com.hemovia.api.service.usecase.request;

import com.hemovia.api.dto.request.CreateBloodRequest;
import com.hemovia.api.service.observability.BusinessMetrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final BusinessMetrics metrics;
    private final ObservationRegistry observations;
    private static final Logger log = LoggerFactory.getLogger(CreateHospitalBloodRequest.class);

    public CreateHospitalBloodRequest(BloodRequestStore store, CurrentUserProvider currentUser, ClockProvider clock,
                                      BusinessMetrics metrics, ObservationRegistry observations) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
        this.metrics = metrics;
        this.observations = observations;
    }

    public BloodRequestResponse execute(CreateBloodRequest request) {
        return Observation.createNotStarted("hemovia.requisicao.criar", observations).observe(() -> create(request));
    }

    private BloodRequestResponse create(CreateBloodRequest request) {
        if (request.deliveryDeadline() == null || !request.deliveryDeadline().isAfter(clock.now())) {
            throw new InvalidDataException("O prazo de entrega deve estar no futuro.");
        }
        var user = currentUser.current();
        if (!user.worksAtHospital(user.hospitalId())) {
            throw new InvalidDataException("Usuário sem vínculo com hospital.");
        }
        var pending = new NewBloodRequest(user.hospitalId(), request.bloodType(), request.rhFactor(),
                request.component(), request.quantity(), request.priority(), request.deliveryDeadline());
        var response = new BloodRequestResponse(store.save(pending), RequestStatus.PENDING);
        metrics.requestIssued();
        log.info("Requisicao de sangue processada");
        return response;
    }
}
