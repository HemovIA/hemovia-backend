package com.hemovia.api.service.usecase.inventory;

import com.hemovia.api.dto.request.BloodBagRequest;
import com.hemovia.api.dto.response.BloodBagResponse;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.exception.BloodBagExpiredException;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ManageBloodBags {
    private final BloodBagRepository repository;
    private final InventoryLinks links;

    public ManageBloodBags(BloodBagRepository repository, InventoryLinks links) {
        this.repository = repository;
        this.links = links;
    }

    @Transactional(readOnly = true)
    public List<BloodBagResponse> list() {
        return repository.findAll().stream().map(InventoryBloodBags::response).toList();
    }

    @Transactional(readOnly = true)
    public BloodBagResponse get(String bloodBagId) {
        return InventoryBloodBags.response(find(bloodBagId));
    }

    public BloodBagResponse create(BloodBagRequest request) {
        LocalDate expiration = validExpiration(request.expirationDate());
        var bag = new BloodBag();
        apply(request, bag, expiration, BloodBagStatus.AVAILABLE);
        return InventoryBloodBags.response(repository.save(bag));
    }

    public BloodBagResponse update(String bloodBagId, BloodBagRequest request) {
        LocalDate expiration = validExpiration(request.expirationDate());
        var bag = find(bloodBagId);
        apply(request, bag, expiration, request.status());
        return InventoryBloodBags.response(repository.save(bag));
    }

    public void delete(String bloodBagId) {
        UUID id = parseId(bloodBagId);
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Blood bag not found: " + bloodBagId);
        }
        repository.deleteById(id);
    }

    private void apply(BloodBagRequest request, BloodBag bag, LocalDate expiration, BloodBagStatus status) {
        InventoryBloodBags.applyDetails(bag, request.bloodTypeEnum(), request.rhFactor(), request.component(),
                toLocalDate(request.collectionDate()), expiration, status);
        bag.setVolumeMl(request.volumeMl());
        bag.setDonor(links.resolvePerson(request.donorId()));
        bag.setBloodCenter(links.resolveBloodCenter(request.bloodCenterId(), bag.getBloodCenter()));
    }

    private BloodBag find(String id) {
        return repository.findById(parseId(id))
                .orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + id));
    }

    private static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid blood bag id format: " + id, e);
        }
    }

    private static LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static LocalDate validExpiration(Date value) {
        LocalDate expiration = toLocalDate(value);
        if (expiration == null || !expiration.isAfter(LocalDate.now(ZoneOffset.UTC))) {
            throw new BloodBagExpiredException();
        }
        return expiration;
    }
}
