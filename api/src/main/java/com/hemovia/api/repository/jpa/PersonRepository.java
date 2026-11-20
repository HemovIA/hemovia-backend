package com.hemovia.api.repository.jpa;

import com.hemovia.api.model.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> { }
