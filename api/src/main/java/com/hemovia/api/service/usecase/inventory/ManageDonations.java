package com.hemovia.api.service.usecase.inventory;

import com.hemovia.api.dto.request.DonationRequest;
import com.hemovia.api.dto.response.DonationResponse;
import com.hemovia.api.model.entity.BloodBag;
import com.hemovia.api.model.entity.Donation;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import com.hemovia.api.repository.jpa.DonationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ManageDonations {
    private final DonationRepository donations;
    private final BloodBagRepository bags;
    private final InventoryLinks links;

    public ManageDonations(DonationRepository donations, BloodBagRepository bags, InventoryLinks links) {
        this.donations = donations;
        this.bags = bags;
        this.links = links;
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> list() {
        return donations.findAll().stream().map(ManageDonations::response).toList();
    }

    @Transactional(readOnly = true)
    public DonationResponse get(String donationId) {
        return response(find(donationId));
    }

    public DonationResponse create(DonationRequest request) {
        var donation = new Donation();
        apply(request, donation);
        BloodBag bag = resolveBloodBag(request);
        if (bag.getDonor() == null) bag.setDonor(donation.getDonor());
        if (bag.getBloodCenter() == null) bag.setBloodCenter(donation.getBloodCenter());
        donation.addBloodBag(bag);
        return response(donations.save(donation));
    }

    public DonationResponse update(String donationId, DonationRequest request) {
        Donation donation = find(donationId);
        apply(request, donation);
        if (request.bloodBagId() != null) {
            BloodBag bag = resolveExistingBag(request.bloodBagId());
            if (bag.getDonation() != null && bag.getDonation() != donation) {
                throw new IllegalArgumentException("Blood bag is already linked to another donation");
            }
            donation.setBloodBag(bag);
        }
        return response(donations.save(donation));
    }

    public void delete(String donationId) {
        Donation donation = find(donationId);
        for (BloodBag bag : List.copyOf(donation.getBloodBags())) {
            donation.removeBloodBag(bag);
        }
        donations.delete(donation);
    }

    private void apply(DonationRequest request, Donation donation) {
        donation.setCollectionCenter(request.collectionCenter());
        donation.setCollectionDate(request.collectionDate());
        donation.setDonor(links.resolvePerson(request.donorId()));
        donation.setBloodCenter(links.resolveBloodCenter(request.bloodCenterId(), donation.getBloodCenter()));
    }

    private BloodBag resolveBloodBag(DonationRequest request) {
        if (request.bloodBagId() != null) return resolveExistingBag(request.bloodBagId());
        var bag = new BloodBag();
        InventoryBloodBags.applyDetails(bag, request.bloodType(), request.rhFactor(), request.component(),
                request.collectionDate(), request.expirationDate(), BloodBagStatus.AVAILABLE);
        return bags.save(bag);
    }

    private BloodBag resolveExistingBag(UUID id) {
        return bags.findById(id).orElseThrow(() -> new EntityNotFoundException("Blood bag not found: " + id));
    }

    private Donation find(String id) {
        return donations.findById(parseId(id))
                .orElseThrow(() -> new EntityNotFoundException("Donation not found: " + id));
    }

    private static UUID parseId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid donation id format: " + id, e);
        }
    }

    private static DonationResponse response(Donation donation) {
        var bloodBags = donation.getBloodBags().stream().map(InventoryBloodBags::donationResponse).toList();
        return new DonationResponse(donation.getId(),
                donation.getCollectionDate() == null ? null : donation.getCollectionDate().toString(),
                donation.getCollectionCenter(), bloodBags.size() == 1 ? bloodBags.getFirst() : null, bloodBags,
                donation.getDonor() == null ? null : donation.getDonor().getId(),
                donation.getBloodCenter() == null ? null : donation.getBloodCenter().getId());
    }
}
