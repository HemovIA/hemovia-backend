package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, UUID> { Optional<Person> findByCpf(String cpf); }

