package com.hemovia.api.service.implementation;

import com.hemovia.api.exceptions.BloodBagExpiredException;
import com.hemovia.api.model.DTOs.BloodBagRequestDTO;
import com.hemovia.api.model.DTOs.BloodBagResponseDTO;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.repository.BloodBagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodBagServiceImplTest {

    @Mock
    private BloodBagRepository repository;

    @InjectMocks
    private BloodBagServiceImpl service;

    @Test
    void shouldRejectExpiredBloodBagBeforeSaving() {
        BloodBagRequestDTO request = requestWithExpiration(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BloodBagExpiredException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldSaveFutureBloodBagAsAvailable() {
        BloodBagRequestDTO request = requestWithExpiration(LocalDate.now().plusDays(1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BloodBagResponseDTO response = service.create(request);
        ArgumentCaptor<com.hemovia.api.model.BloodBag> captor =
                ArgumentCaptor.forClass(com.hemovia.api.model.BloodBag.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BloodBagStatus.AVAILABLE);
        assertThat(response.getExpirationDate()).isNotNull();
    }

    private BloodBagRequestDTO requestWithExpiration(LocalDate expirationDate) {
        BloodBagRequestDTO request = new BloodBagRequestDTO();
        request.setRhFactor("POSITIVE");
        request.setComponent("WHOLE_BLOOD");
        request.setCollectionDate(toDate(LocalDate.now()));
        request.setExpirationDate(toDate(expirationDate));
        request.setStatus(BloodBagStatus.DISCARDED);
        request.setBloodTypeEnum(BloodType.O);
        return request;
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}