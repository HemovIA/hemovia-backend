package com.hemovia.api.service.implementation;

import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.Donation;
import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.model.DTOs.DonationRequestDTO;
import com.hemovia.api.model.DTOs.DonationResponseDTO;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.repository.DonationRepository;
import com.hemovia.api.service.DonationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final BloodBagRepository bloodBagRepository;

    public DonationServiceImpl(DonationRepository donationRepository, BloodBagRepository bloodBagRepository) {
        this.donationRepository = donationRepository;
        this.bloodBagRepository = bloodBagRepository;
    }

    @Override
    public List<DonationResponseDTO> getAllDonations() {
        return donationRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public DonationResponseDTO getDonationById(String donationId) {
        UUID id = parseId(donationId);
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found: " + donationId));
        return toResponseDTO(donation);
    }

    @Override
    public DonationResponseDTO create(DonationRequestDTO donationRequestDTO) {
        BloodBag bloodBag = resolveBloodBag(donationRequestDTO);

        Donation donation = new Donation();
        donation.setCollectionCenter(donationRequestDTO.getCollectionCenter());
        donation.setCollectionDate(donationRequestDTO.getCollectionDate());
        donation.setBloodBag(bloodBag);

        return toResponseDTO(donationRepository.save(donation));
    }

    @Override
    public DonationResponseDTO update(String donationId, DonationRequestDTO donationRequestDTO) {
        UUID id = parseId(donationId);
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Donation not found: " + donationId));

        donation.setCollectionCenter(donationRequestDTO.getCollectionCenter());
        donation.setCollectionDate(donationRequestDTO.getCollectionDate());

        BloodBag currentBloodBag = resolveBloodBag(donationRequestDTO, donation.getBloodBag());
        donation.setBloodBag(currentBloodBag);

        return toResponseDTO(donationRepository.save(donation));
    }

    @Override
    public void delete(String donationId) {
        UUID id = parseId(donationId);
        if (!donationRepository.existsById(id)) {
            throw new EntityNotFoundException("Donation not found: " + donationId);
        }
        donationRepository.deleteById(id);
    }

    private BloodBag resolveBloodBag(DonationRequestDTO requestDTO) {
        return resolveBloodBag(requestDTO, null);
    }

    private BloodBag resolveBloodBag(DonationRequestDTO requestDTO, BloodBag existingBloodBag) {
        if (requestDTO.getBloodBagId() != null) {
            return bloodBagRepository.findById(requestDTO.getBloodBagId())
                    .orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + requestDTO.getBloodBagId()));
        }

        if (existingBloodBag != null) {
            return existingBloodBag;
        }

        BloodBag bloodBag = new BloodBag();
        bloodBag.setRhFactor(requestDTO.getRhFactor());
        bloodBag.setComponent(requestDTO.getComponent());
        bloodBag.setCollectionDate(toDate(requestDTO.getCollectionDate()));
        bloodBag.setExpirationDate(toDate(requestDTO.getExpirationDate()));
        bloodBag.setStatus(BloodBagStatus.AVAILABLE);
        bloodBag.setBloodTypeEnum(requestDTO.getBloodType());
        return bloodBagRepository.save(bloodBag);
    }

    private DonationResponseDTO toResponseDTO(Donation donation) {
        DonationResponseDTO response = new DonationResponseDTO();
        response.setDonationId(donation.getId());
        response.setCollectionDate(donation.getCollectionDate());
        response.setCollectionCenter(donation.getCollectionCenter());

        if (donation.getBloodBag() != null) {
            BloodBag bloodBag = donation.getBloodBag();
            BloodBagResponseDTO bloodBagResponse = new BloodBagResponseDTO();
            bloodBagResponse.setBloodBagId(bloodBag.getId());
            bloodBagResponse.setBloodType(bloodBag.getBloodTypeEnum() != null ? bloodBag.getBloodTypeEnum().name() : null);
            bloodBagResponse.setRhFactor(bloodBag.getRhFactor());
            bloodBagResponse.setExpirationDate(bloodBag.getExpirationDate() != null ? bloodBag.getExpirationDate().toString() : null);
            response.setBloodBag(bloodBagResponse);
        }

        return response;
    }

    private UUID parseId(String donationId) {
        try {
            return UUID.fromString(donationId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid donation id format: " + donationId, e);
        }
    }

    private Date toDate(LocalDate localDate) {
        return localDate == null ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
