package com.hemovia.api.service.usecase.donor;
import com.hemovia.api.dto.request.DonorRequest;
import com.hemovia.api.dto.response.DonorResponse;
import com.hemovia.api.model.entity.Person;
import com.hemovia.api.model.exception.DuplicateResourceException;
import com.hemovia.api.model.exception.ResourceNotFoundException;
import com.hemovia.api.model.valueobject.Cpf;
import com.hemovia.api.repository.jpa.PersonRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;

@Service
public class ManageDonors {
    private final PersonRepository people;
    public ManageDonors(PersonRepository people) { this.people = people; }
    public DonorResponse find(String cpf) {
        return response(people.findByCpf(new Cpf(cpf).digits()).orElseThrow(() -> new ResourceNotFoundException("Doador", "CPF informado")));
    }
    public DonorResponse create(DonorRequest request) {
        String cpf = new Cpf(request.cpf()).digits();
        Person person = new Person(null, request.name(), request.email(), request.phoneNumber());
        person.setCpf(cpf);
        try { return response(people.saveAndFlush(person)); }
        catch (DataIntegrityViolationException ex) {
            for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation && "uk_person_cpf".equals(violation.getConstraintName()))
                    throw new DuplicateResourceException("CPF já cadastrado");
            }
            throw ex;
        }
    }
    private DonorResponse response(Person person) {
        return new DonorResponse(person.getId(), person.getName(), person.getCpf(), person.getEmail(), person.getPhoneNumber());
    }
}


