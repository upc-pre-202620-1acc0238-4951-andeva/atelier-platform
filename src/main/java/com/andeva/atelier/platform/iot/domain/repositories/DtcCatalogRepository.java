package com.andeva.atelier.platform.iot.domain.repositories;

import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;

import java.util.List;
import java.util.Optional;

/**
 * Outbound domain repository port for querying standardized DTC master catalog entries.
 *
 * @author Joel Huamani Estefanero
 */
public interface DtcCatalogRepository {

    Optional<DtcCatalogEntry> findByCode(DtcCode code);

    List<DtcCatalogEntry> findAllByCategory(String systemCategory);

    boolean existsByCode(DtcCode code);

    DtcCatalogEntry save(DtcCatalogEntry entry);

    List<DtcCatalogEntry> findAll();
}
