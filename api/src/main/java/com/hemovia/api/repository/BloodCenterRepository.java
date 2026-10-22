package com.hemovia.api.repository;

import com.hemovia.api.model.BloodCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BloodCenterRepository extends JpaRepository<BloodCenter, UUID> { }
