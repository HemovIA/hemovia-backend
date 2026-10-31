package com.hemovia.api.repository;

import com.hemovia.api.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> { }
