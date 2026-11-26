package com.hemovia.api.service.usecase.donor;

import com.hemovia.api.dto.request.DonorRequest;
import com.hemovia.api.model.entity.Person;
import com.hemovia.api.model.exception.InvalidDataException;
import com.hemovia.api.repository.jpa.PersonRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ManageDonorsTest {
    private final PersonRepository repository = mock(PersonRepository.class);
    private final ManageDonors service = new ManageDonors(repository);

    @Test @DisplayName("@spec:AC-094 cadastra CPF normalizado com contatos opcionais")
    void createsWithoutContacts() {
        when(repository.saveAndFlush(any(Person.class))).thenAnswer(call -> {
            Person person = call.getArgument(0); person.setId(UUID.randomUUID()); return person;
        });
        var donor = service.create(new DonorRequest("Doador", "529.982.247-25", null, null));
        assertEquals("52998224725", donor.cpf());
        assertNull(donor.email()); assertNull(donor.phoneNumber()); assertNotNull(donor.id());
    }

    @Test @DisplayName("@spec:AC-095 consulta somente CPF completo e válido")
    void searchesExactNormalizedCpf() {
        Person person = new Person(UUID.randomUUID(), "Doador", null, null); person.setCpf("52998224725");
        when(repository.findByCpf("52998224725")).thenReturn(Optional.of(person));
        assertEquals(person.getId(), service.find("529.982.247-25").id());
        assertThrows(InvalidDataException.class, () -> service.find("529"));
        verify(repository, times(1)).findByCpf(any());
    }
}
