package com.hemovia.api.repository;

import com.hemovia.api.model.BloodCenter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BloodCenterRepository extends JpaRepository<BloodCenter, UUID> {
    List<BloodCenter> findAllByDeletedAtIsNullOrderByNameAscIdAsc();

    Optional<BloodCenter> findByIdAndDeletedAtIsNull(UUID id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select center from BloodCenter center where center.id = :id and center.deletedAt is null")
    Optional<BloodCenter> findActiveForUse(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select center from BloodCenter center where center.id = :id and center.deletedAt is null")
    Optional<BloodCenter> findActiveForUpdate(@Param("id") UUID id);
}
