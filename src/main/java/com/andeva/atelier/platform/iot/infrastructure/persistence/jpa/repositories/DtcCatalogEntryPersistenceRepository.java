package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.entities.DtcCatalogEntryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link DtcCatalogEntryPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface DtcCatalogEntryPersistenceRepository extends JpaRepository<DtcCatalogEntryPersistenceEntity, UUID> {

    Optional<DtcCatalogEntryPersistenceEntity> findByDtcCode(String dtcCode);

    List<DtcCatalogEntryPersistenceEntity> findAllBySystemCategory(DtcCategory systemCategory);

    boolean existsByDtcCode(String dtcCode);
}
