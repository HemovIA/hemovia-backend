package com.hemovia.api.service.implementation;

import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.BloodCenter;
import com.hemovia.api.model.Donation;
import com.hemovia.api.model.Person;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.model.DTOs.DonationRequestDTO;
import com.hemovia.api.model.DTOs.DonationResponseDTO;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodComponent;
import com.hemovia.api.model.enums.RhFactor;
import com.hemovia.api.repository.BloodBagRepository;
import com.hemovia.api.repository.BloodCenterRepository;
import com.hemovia.api.repository.DonationRepository;
import com.hemovia.api.repository.PersonRepository;
import com.hemovia.api.service.DonationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DonationServiceImpl implements DonationService {
    private final DonationRepository donationRepository;
    private final BloodBagRepository bloodBagRepository;
    private final PersonRepository personRepository;
    private final BloodCenterRepository bloodCenterRepository;

    public DonationServiceImpl(DonationRepository donationRepository, BloodBagRepository bloodBagRepository, PersonRepository personRepository, BloodCenterRepository bloodCenterRepository) {
        this.donationRepository = donationRepository; this.bloodBagRepository = bloodBagRepository; this.personRepository = personRepository; this.bloodCenterRepository = bloodCenterRepository;
    }
    @Override @Transactional(readOnly = true) public List<DonationResponseDTO> getAllDonations() { return donationRepository.findAll().stream().map(this::toResponseDTO).toList(); }
    @Override @Transactional(readOnly = true) public DonationResponseDTO getDonationById(String donationId) { return toResponseDTO(find(parseId(donationId), donationId)); }
    @Override public DonationResponseDTO create(DonationRequestDTO request) {
        Donation donation = new Donation(); apply(request, donation); BloodBag bag = resolveBloodBag(request);
        if (bag.getDonor() == null) bag.setDonor(donation.getDonor());
        if (bag.getBloodCenter() == null) bag.setBloodCenter(donation.getBloodCenter());
        donation.addBloodBag(bag); return toResponseDTO(donationRepository.save(donation));
    }
    @Override public DonationResponseDTO update(String donationId, DonationRequestDTO request) {
        Donation donation = find(parseId(donationId), donationId); apply(request, donation);
        if (request.getBloodBagId() != null) { BloodBag bag = resolveExistingBag(request.getBloodBagId()); if (bag.getDonation() != null && bag.getDonation() != donation) throw new IllegalArgumentException("Blood bag is already linked to another donation"); donation.setBloodBag(bag); }
        return toResponseDTO(donationRepository.save(donation));
    }
    @Override public void delete(String donationId) {
        Donation donation = find(parseId(donationId), donationId);
        for (BloodBag bag : List.copyOf(donation.getBloodBags())) donation.removeBloodBag(bag);
        donationRepository.delete(donation);
    }

    private void apply(DonationRequestDTO request, Donation donation) {
        donation.setCollectionCenter(request.getCollectionCenter()); donation.setCollectionDate(request.getCollectionDate());
        donation.setDonor(resolvePerson(request.getDonorId())); donation.setBloodCenter(resolveBloodCenter(request.getBloodCenterId()));
    }
    private BloodBag resolveBloodBag(DonationRequestDTO request) {
        if (request.getBloodBagId() != null) return resolveExistingBag(request.getBloodBagId());
        BloodBag bag = new BloodBag(); bag.setRhFactor(parseRhFactor(request.getRhFactor())); bag.setComponent(parseComponent(request.getComponent()));
        bag.setCollectionDate(request.getCollectionDate()); bag.setExpirationDate(request.getExpirationDate()); bag.setStatus(BloodBagStatus.AVAILABLE); bag.setBloodTypeEnum(request.getBloodType());
        return bloodBagRepository.save(bag);
    }
    private BloodBag resolveExistingBag(UUID id) { return bloodBagRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + id)); }
    private Person resolvePerson(UUID id) { return id == null ? null : personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found: " + id)); }
    private BloodCenter resolveBloodCenter(UUID id) { return id == null ? null : bloodCenterRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Blood center not found: " + id)); }
    private Donation find(UUID id, String raw) { return donationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Donation not found: " + raw)); }
    private UUID parseId(String id) { try { return UUID.fromString(id); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid donation id format: " + id, e); } }
    private RhFactor parseRhFactor(String value) { try { return RhFactor.valueOf(value); } catch (RuntimeException e) { throw new IllegalArgumentException("Unknown Rh factor: " + value, e); } }
    private BloodComponent parseComponent(String value) { try { return BloodComponent.valueOf(value); } catch (RuntimeException e) { throw new IllegalArgumentException("Unknown blood component: " + value, e); } }
    private DonationResponseDTO toResponseDTO(Donation donation) {
        DonationResponseDTO response = new DonationResponseDTO(); response.setDonationId(donation.getId()); response.setCollectionDate(donation.getCollectionDate()); response.setCollectionCenter(donation.getCollectionCenter());
        response.setDonorId(donation.getDonor() == null ? null : donation.getDonor().getId()); response.setBloodCenterId(donation.getBloodCenter() == null ? null : donation.getBloodCenter().getId());
        List<BloodBagResponseDTO> bags = donation.getBloodBags().stream().map(this::bagResponse).toList(); response.setBloodBags(bags); if (bags.size() == 1) response.setBloodBag(bags.getFirst()); return response;
    }
    private BloodBagResponseDTO bagResponse(BloodBag bag) {
        BloodBagResponseDTO response = new BloodBagResponseDTO(); response.setBloodBagId(bag.getId()); response.setBloodType(bag.getBloodTypeEnum() == null ? null : bag.getBloodTypeEnum().name()); response.setRhFactor(bag.getRhFactor() == null ? null : bag.getRhFactor().name()); response.setComponent(bag.getComponent() == null ? null : bag.getComponent().name()); response.setCollectionDate(bag.getCollectionDate() == null ? null : bag.getCollectionDate().toString()); response.setExpirationDate(bag.getExpirationDate() == null ? null : bag.getExpirationDate().toString()); return response;
    }
}
