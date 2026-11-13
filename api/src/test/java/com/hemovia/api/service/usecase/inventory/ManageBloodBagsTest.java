package com.hemovia.api.service.usecase.inventory;

import com.hemovia.api.model.exception.BloodBagExpiredException;
import com.hemovia.api.dto.request.BloodBagRequest;
import com.hemovia.api.dto.response.BloodBagResponse;
import com.hemovia.api.model.enums.BloodBagStatus;
import com.hemovia.api.model.enums.BloodType;
import com.hemovia.api.repository.jpa.BloodBagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageBloodBagsTest {

    @Mock
    private BloodBagRepository repository;

    @Mock
    private InventoryLinks links;

    @InjectMocks
    private ManageBloodBags service;

    @Test
    void shouldRejectExpiredBloodBagBeforeSaving() {
        BloodBagRequest request = requestWithExpiration(LocalDate.now(ZoneOffset.UTC).minusDays(1));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BloodBagExpiredException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void shouldSaveFutureBloodBagAsAvailable() {
        BloodBagRequest request = requestWithExpiration(LocalDate.now(ZoneOffset.UTC).plusDays(1));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BloodBagResponse response = service.create(request);
        ArgumentCaptor<com.hemovia.api.model.entity.BloodBag> captor =
                ArgumentCaptor.forClass(com.hemovia.api.model.entity.BloodBag.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BloodBagStatus.AVAILABLE);
        assertThat(response.expirationDate()).isNotNull();
    }

    private BloodBagRequest requestWithExpiration(LocalDate expirationDate) {
        return new BloodBagRequest("POSITIVE", "WHOLE_BLOOD", toDate(LocalDate.now(ZoneOffset.UTC)),
                toDate(expirationDate), BloodBagStatus.DISCARDED, BloodType.O, null, null, null);
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    }
}
