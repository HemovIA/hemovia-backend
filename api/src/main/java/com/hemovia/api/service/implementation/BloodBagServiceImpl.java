package com.hemovia.api.service.implementation;

import com.hemovia.api.exceptions.BloodBagExpiredException;
import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.service.BloodBagService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;
import java.util.UUID;

@Service
public class BloodBagServiceImpl implements BloodBagService {

    private final BloodBagRepository repository;

    public BloodBagServiceImpl(BloodBagRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<BloodBagResponseDTO> getAllBloodBags() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public BloodBagResponseDTO getBloodBagById(String bloodBagId) {
        UUID id = parseId(bloodBagId);
        BloodBag bloodBag = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + bloodBagId));
        return toResponseDTO(bloodBag);
    }

    @Override
    public BloodBagResponseDTO create(BloodBagRequestDTO bloodBagRequestDTO) {
        validateExpirationDate(bloodBagRequestDTO.getExpirationDate());

        BloodBag bloodBag = new BloodBag();
        bloodBag.setRhFactor(bloodBagRequestDTO.getRhFactor());
        bloodBag.setComponent(bloodBagRequestDTO.getComponent());
        bloodBag.setCollectionDate(bloodBagRequestDTO.getCollectionDate());
        bloodBag.setExpirationDate(bloodBagRequestDTO.getExpirationDate());
        bloodBag.setStatus(BloodBagStatus.AVAILABLE);
        bloodBag.setBloodTypeEnum(bloodBagRequestDTO.getBloodTypeEnum());

        return toResponseDTO(repository.save(bloodBag));
    }

    @Override
    public BloodBagResponseDTO update(String bloodBagId, BloodBagRequestDTO bloodBagRequestDTO) {
        UUID id = parseId(bloodBagId);
        BloodBag bloodBag = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + bloodBagId));
        validateExpirationDate(bloodBagRequestDTO.getExpirationDate());

        bloodBag.setRhFactor(bloodBagRequestDTO.getRhFactor());
        bloodBag.setComponent(bloodBagRequestDTO.getComponent());
        bloodBag.setCollectionDate(bloodBagRequestDTO.getCollectionDate());
        bloodBag.setExpirationDate(bloodBagRequestDTO.getExpirationDate());
        bloodBag.setStatus(bloodBagRequestDTO.getStatus());
        bloodBag.setBloodTypeEnum(bloodBagRequestDTO.getBloodTypeEnum());

        return toResponseDTO(repository.save(bloodBag));
    }

    @Override
    public void delete(String bloodBagId) {
        UUID id = parseId(bloodBagId);
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Blood bag not found: " + bloodBagId);
        }
        repository.deleteById(id);
    }

    private BloodBagResponseDTO toResponseDTO(BloodBag bloodBag) {
        BloodBagResponseDTO responseDTO = new BloodBagResponseDTO();
        responseDTO.setBloodBagId(bloodBag.getId());
        responseDTO.setBloodType(bloodBag.getBloodTypeEnum() != null ? bloodBag.getBloodTypeEnum().name() : null);
        responseDTO.setRhFactor(bloodBag.getRhFactor());
        responseDTO.setExpirationDate(bloodBag.getExpirationDate() != null ? bloodBag.getExpirationDate().toString() : null);
        return responseDTO;
    }

    private UUID parseId(String bloodBagId) {
        try {
            return UUID.fromString(bloodBagId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid blood bag id format: " + bloodBagId, e);
        }
    }

    private void validateExpirationDate(Date expirationDate) {
        LocalDate expiration = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        if (!expiration.isAfter(LocalDate.now())) {
            throw new BloodBagExpiredException();
        }
    }
}
