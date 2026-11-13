package com.hemovia.api.service;

import com.hemovia.api.dto.request.DonationRequestDTO;
import com.hemovia.api.dto.response.DonationResponseDTO;

import java.util.List;

public interface DonationService {

    List<DonationResponseDTO> getAllDonations();

    DonationResponseDTO getDonationById(String donationId);

    DonationResponseDTO create (DonationRequestDTO donationRequestDTO);

    DonationResponseDTO update(String donationId, DonationRequestDTO donationRequestDTO);

    void delete(String donationId);
}
