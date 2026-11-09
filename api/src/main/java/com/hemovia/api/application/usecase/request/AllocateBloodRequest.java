package com.hemovia.api.application.usecase.request;

import com.hemovia.api.application.dto.response.AllocationResponse;
import com.hemovia.api.application.port.input.AllocateBloodRequestUseCase;
import com.hemovia.api.application.port.output.ClockProvider;
import com.hemovia.api.application.port.output.CurrentUserProvider;
import com.hemovia.api.application.port.output.RequestAllocationStore;
import com.hemovia.api.domain.BloodCompatibility;
import com.hemovia.api.domain.algorithm.priority.FefoQueue;
import com.hemovia.api.domain.exception.InsufficientInventoryException;
import com.hemovia.api.domain.exception.InvalidDataException;
import com.hemovia.api.domain.exception.InvalidStateTransitionException;
import com.hemovia.api.domain.exception.RequestAlreadyProcessedException;
import com.hemovia.api.domain.exception.ResourceNotFoundException;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.RequestStatus;
import com.hemovia.api.model.enums.RhFactor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class AllocateBloodRequest implements AllocateBloodRequestUseCase {
    private final RequestAllocationStore store;
    private final CurrentUserProvider currentUser;
    private final ClockProvider clock;

    public AllocateBloodRequest(RequestAllocationStore store, CurrentUserProvider currentUser, ClockProvider clock) {
        this.store = store;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AllocationResponse execute(UUID requestId) {
        var user = currentUser.current();
        if (!user.worksAtBloodCenter(user.bloodCenterId())) {
            throw new InvalidDataException("É necessário um vínculo com um hemocentro para alocar a requisição.");
        }
        var request = store.lockRequest(requestId).orElseThrow(() -> new ResourceNotFoundException("Requisição", requestId));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RequestAlreadyProcessedException();
        }
        if (request.getComponent() != BloodComponent.RED_BLOOD_CELLS) {
            throw new InvalidStateTransitionException("Somente requisições de hemácias podem ser alocadas.");
        }

        var today = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
        boolean recipientRhPositive = request.getRhFactor() == RhFactor.POSITIVE;
        var compatible = store.lockEligibleBags(user.bloodCenterId(), request.getComponent(), today).stream()
                .filter(bag -> BloodCompatibility.canTransfuse(bag.getBloodTypeEnum(),
                        bag.getRhFactor() == RhFactor.POSITIVE, request.getBloodType(), recipientRhPositive))
                .toList();
        var queue = FefoQueue.prepare(compatible, today);
        int quantity = request.getQuantity();
        if (queue.size() < quantity) {
            throw new InsufficientInventoryException();
        }

        var selected = new ArrayList<BloodBag>(quantity);
        for (int i = 0; i < quantity; i++) {
            selected.add(queue.remove());
        }
        var allocation = store.reserve(request, user.bloodCenterId(), selected);
        return new AllocationResponse(allocation.getId(), requestId, request.getStatus(),
                selected.stream().map(BloodBag::getId).toList());
    }
}
