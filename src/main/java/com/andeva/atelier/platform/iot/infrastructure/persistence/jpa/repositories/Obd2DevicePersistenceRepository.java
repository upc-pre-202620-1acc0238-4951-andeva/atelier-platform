package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.Obd2DevicePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Obd2DevicePersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface Obd2DevicePersistenceRepository extends JpaRepository<Obd2DevicePersistenceEntity, UUID> {

    Optional<Obd2DevicePersistenceEntity> findByDeviceIdentifier(String deviceIdentifier);

    List<Obd2DevicePersistenceEntity> findAllByTenantId(UUID tenantId);

    boolean existsByDeviceIdentifier(String deviceIdentifier);
}
