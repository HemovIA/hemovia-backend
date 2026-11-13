package com.hemovia.api.service.usecase.inventory;

import com.hemovia.api.model.entity.BloodCenter;
import com.hemovia.api.model.entity.Person;
import com.hemovia.api.repository.jpa.BloodCenterRepository;
import com.hemovia.api.repository.jpa.PersonRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InventoryLinks {
    private final PersonRepository people;
    private final BloodCenterRepository centers;

    public InventoryLinks(PersonRepository people, BloodCenterRepository centers) {
        this.people = people;
        this.centers = centers;
    }

    Person resolvePerson(UUID id) {
        return id == null ? null : people.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found: " + id));
    }

    BloodCenter resolveBloodCenter(UUID id, BloodCenter existing) {
        if (id == null) return null;
        if (existing != null && id.equals(existing.getId())) return existing;
        return centers.findActiveForUse(id)
                .orElseThrow(() -> new EntityNotFoundException("Blood center not found: " + id));
    }
}
