package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.VehicleOwnershipPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleOwnershipPersistenceRepository extends JpaRepository<VehicleOwnershipPersistenceEntity, UUID> {

    @Query("SELECT o FROM VehicleOwnershipPersistenceEntity o " +
           "WHERE o.vehicle.id = :vehicleId AND o.endDate IS NULL")
    Optional<VehicleOwnershipPersistenceEntity> findActiveByVehicleId(@Param("vehicleId") UUID vehicleId);

    List<VehicleOwnershipPersistenceEntity> findAllByVehicleIdOrderByStartDateDesc(UUID vehicleId);

    List<VehicleOwnershipPersistenceEntity> findAllByCustomerIdAndEndDateIsNull(UUID customerId);

    List<VehicleOwnershipPersistenceEntity> findAllByUserIdAndEndDateIsNull(UUID userId);
}
