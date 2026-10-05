package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehiclePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehiclePersistenceRepository extends JpaRepository<VehiclePersistenceEntity, UUID> {

    boolean existsByPlate(String plate);

    boolean existsByVin(String vin);

    Optional<VehiclePersistenceEntity> findByPlate(String plate);

    Optional<VehiclePersistenceEntity> findByVin(String vin);

    @Query("SELECT v FROM VehiclePersistenceEntity v " +
           "JOIN v.ownershipHistory o " +
           "WHERE o.customerId = :customerId AND o.endDate IS NULL")
    List<VehiclePersistenceEntity> findAllActiveByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT v FROM VehiclePersistenceEntity v " +
           "JOIN v.ownershipHistory o " +
           "WHERE o.userId = :userId AND o.endDate IS NULL")
    List<VehiclePersistenceEntity> findAllActiveByUserId(@Param("userId") UUID userId);
}
