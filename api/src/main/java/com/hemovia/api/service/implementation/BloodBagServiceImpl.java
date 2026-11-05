package com.hemovia.api.service.implementation;

import com.hemovia.api.domain.exception.BloodBagExpiredException;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.BloodCenter;
import com.hemovia.api.model.Person;
import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.RhFactor;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.repository.BloodCenterRepository;
import com.hemovia.api.repository.PersonRepository;
import com.hemovia.api.service.BloodBagService;
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
public class BloodBagServiceImpl implements BloodBagService {
    private final BloodBagRepository repository;
    private final PersonRepository personRepository;
    private final BloodCenterRepository bloodCenterRepository;

    public BloodBagServiceImpl(BloodBagRepository repository, PersonRepository personRepository, BloodCenterRepository bloodCenterRepository) {
        this.repository = repository; this.personRepository = personRepository; this.bloodCenterRepository = bloodCenterRepository;
    }
    @Override @Transactional(readOnly = true) public List<BloodBagResponseDTO> getAllBloodBags() { return repository.findAll().stream().map(this::toResponseDTO).toList(); }
    @Override @Transactional(readOnly = true) public BloodBagResponseDTO getBloodBagById(String bloodBagId) { return toResponseDTO(find(parseId(bloodBagId), bloodBagId)); }
    @Override public BloodBagResponseDTO create(BloodBagRequestDTO request) { validateExpirationDate(request.getExpirationDate()); BloodBag bag = new BloodBag(); apply(request, bag, true); return toResponseDTO(repository.save(bag)); }
    @Override public BloodBagResponseDTO update(String bloodBagId, BloodBagRequestDTO request) { validateExpirationDate(request.getExpirationDate()); BloodBag bag = find(parseId(bloodBagId), bloodBagId); apply(request, bag, false); return toResponseDTO(repository.save(bag)); }
    @Override public void delete(String bloodBagId) { UUID id = parseId(bloodBagId); if (!repository.existsById(id)) throw new EntityNotFoundException("Blood bag not found: " + bloodBagId); repository.deleteById(id); }

    private void apply(BloodBagRequestDTO request, BloodBag bag, boolean creating) {
        bag.setRhFactor(parseRhFactor(request.getRhFactor())); bag.setComponent(parseComponent(request.getComponent()));
        bag.setCollectionDate(toLocalDate(request.getCollectionDate())); bag.setExpirationDate(toLocalDate(request.getExpirationDate()));
        bag.setStatus(creating ? BloodBagStatus.AVAILABLE : request.getStatus()); bag.setBloodTypeEnum(request.getBloodTypeEnum());
        bag.setVolumeMl(request.getVolumeMl()); bag.setDonor(resolvePerson(request.getDonorId())); bag.setBloodCenter(resolveBloodCenter(request.getBloodCenterId()));
    }
    private Person resolvePerson(UUID id) { return id == null ? null : personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found: " + id)); }
    private BloodCenter resolveBloodCenter(UUID id) { return id == null ? null : bloodCenterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Blood center not found: " + id)); }
    private BloodBag find(UUID id, String rawId) { return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + rawId)); }
    private RhFactor parseRhFactor(String value) { try { return RhFactor.valueOf(value); } catch (RuntimeException e) { throw new IllegalArgumentException("Unknown Rh factor: " + value, e); } }
    private BloodComponent parseComponent(String value) { try { return BloodComponent.valueOf(value); } catch (RuntimeException e) { throw new IllegalArgumentException("Unknown blood component: " + value, e); } }
    private UUID parseId(String id) { try { return UUID.fromString(id); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid blood bag id format: " + id, e); } }
    private LocalDate toLocalDate(Date value) { return value == null ? null : value.toInstant().atZone(ZoneOffset.UTC).toLocalDate(); }
    private void validateExpirationDate(Date value) { if (value == null || !toLocalDate(value).isAfter(LocalDate.now(ZoneOffset.UTC))) throw new BloodBagExpiredException(); }
    private BloodBagResponseDTO toResponseDTO(BloodBag bag) {
        BloodBagResponseDTO response = new BloodBagResponseDTO(); response.setBloodBagId(bag.getId()); response.setBloodType(bag.getBloodTypeEnum() == null ? null : bag.getBloodTypeEnum().name());
        response.setRhFactor(bag.getRhFactor() == null ? null : bag.getRhFactor().name()); response.setComponent(bag.getComponent() == null ? null : bag.getComponent().name());
        response.setCollectionDate(bag.getCollectionDate() == null ? null : bag.getCollectionDate().toString()); response.setExpirationDate(bag.getExpirationDate() == null ? null : bag.getExpirationDate().toString());
        response.setStatus(bag.getStatus() == null ? null : bag.getStatus().name()); response.setDonorId(bag.getDonor() == null ? null : bag.getDonor().getId()); response.setBloodCenterId(bag.getBloodCenter() == null ? null : bag.getBloodCenter().getId()); return response;
    }
}
