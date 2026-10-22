package com.hemovia.api.service.implementation;

import com.hemovia.api.model.BloodBag;
import com.hemovia.api.model.BloodCenter;
import com.hemovia.api.model.Donation;
import com.hemovia.api.model.DTOs.DonationRequestDTO;
import com.hemovia.api.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DonationAssociationsRegressionTest {
    @Test @DisplayName("@spec:AC-007 hemocentros conhecidos divergentes rejeitam associação e alteração")
    void ac007_rejectsConflictingKnownCenters() {
        BloodCenter origin = new BloodCenter(), other = new BloodCenter();
        Donation donation = new Donation(); donation.setBloodCenter(origin);
        BloodBag bag = new BloodBag(); bag.setBloodCenter(other);
        assertThrows(IllegalArgumentException.class, () -> donation.addBloodBag(bag));
        assertNull(bag.getDonation());
        bag.setBloodCenter(origin); donation.addBloodBag(bag);
        assertThrows(IllegalArgumentException.class, () -> donation.setBloodCenter(other));
        assertThrows(IllegalArgumentException.class, () -> bag.setBloodCenter(other));
        assertSame(origin, donation.getBloodCenter());
        assertSame(origin, bag.getBloodCenter());
    }

    @Test @DisplayName("@spec:AC-007 associar bolsa de outra doação rejeita transferência silenciosa")
    void ac007_cannotStealBagFromAnotherDonation() {
        Donation first = new Donation(), second = new Donation();
        BloodBag bag = new BloodBag(); first.addBloodBag(bag);
        assertThrows(IllegalArgumentException.class, () -> second.addBloodBag(bag));
        assertSame(first, bag.getDonation());
        assertEquals(1, first.getBloodBags().size());
        assertTrue(second.getBloodBags().isEmpty());
    }

    @Test @DisplayName("@spec:AC-015 alteração singular preserva coleção com várias bolsas")
    void ac015_singularUpdatePreservesMultipleBags() {
        Donation donation = new Donation();
        BloodBag first = new BloodBag(), second = new BloodBag();
        donation.addBloodBag(first); donation.addBloodBag(second);
        donation.setBloodBag(first);
        assertEquals(2, donation.getBloodBags().size());
        assertSame(donation, second.getDonation());
        assertThrows(IllegalArgumentException.class, () -> donation.setBloodBag(new BloodBag()));
        assertEquals(2, donation.getBloodBags().size());
    }

    @Test @DisplayName("@spec:AC-015 criar doação com bolsa já associada falha sem salvar nova doação")
    void ac015_createRejectsAlreadyAssociatedBag() {
        DonationRepository donations = mock(DonationRepository.class);
        BloodBagRepository bags = mock(BloodBagRepository.class);
        var service = new DonationServiceImpl(donations, bags, mock(PersonRepository.class), mock(BloodCenterRepository.class));
        BloodBag bag = new BloodBag(); bag.setId(UUID.randomUUID());
        Donation original = new Donation(); original.addBloodBag(bag);
        when(bags.findById(bag.getId())).thenReturn(Optional.of(bag));
        DonationRequestDTO request = new DonationRequestDTO();
        request.setCollectionCenter("Centro Sintético"); request.setCollectionDate(LocalDate.of(2030, 1, 1));
        request.setBloodBagId(bag.getId());
        assertThrows(IllegalArgumentException.class, () -> service.create(request));
        verify(donations, never()).save(any());
        assertSame(original, bag.getDonation());
    }
}
