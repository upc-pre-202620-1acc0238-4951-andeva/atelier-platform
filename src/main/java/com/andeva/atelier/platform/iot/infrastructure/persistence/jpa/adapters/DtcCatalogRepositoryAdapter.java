package com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.repositories.DtcCatalogRepository;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.assemblers.DtcCatalogPersistenceAssembler;
import com.andeva.atelier.platform.iot.infrastructure.persistence.jpa.repositories.DtcCatalogEntryPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing {@link DtcCatalogRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public class DtcCatalogRepositoryAdapter implements DtcCatalogRepository {

    private final DtcCatalogEntryPersistenceRepository persistenceRepository;
    private final DtcCatalogPersistenceAssembler assembler;

    public DtcCatalogRepositoryAdapter(
            DtcCatalogEntryPersistenceRepository persistenceRepository,
            DtcCatalogPersistenceAssembler assembler) {
        this.persistenceRepository = persistenceRepository;
        this.assembler = assembler;
    }

    @Override
    public Optional<DtcCatalogEntry> findByCode(DtcCode code) {
        Objects.requireNonNull(code, "code cannot be null");
        return persistenceRepository.findByDtcCode(code.value()).map(assembler::toDomain);
    }

    @Override
    public List<DtcCatalogEntry> findAllByCategory(String systemCategory) {
        Objects.requireNonNull(systemCategory, "systemCategory cannot be null");
        try {
            DtcCategory category = DtcCategory.valueOf(systemCategory.trim().toUpperCase());
            return persistenceRepository.findAllBySystemCategory(category).stream()
                    .map(assembler::toDomain)
                    .toList();
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean existsByCode(DtcCode code) {
        Objects.requireNonNull(code, "code cannot be null");
        return persistenceRepository.existsByDtcCode(code.value());
    }

    @Override
    public DtcCatalogEntry save(DtcCatalogEntry entry) {
        Objects.requireNonNull(entry, "entry cannot be null");
        return assembler.toDomain(persistenceRepository.save(assembler.toEntity(entry)));
    }

    @Override
    public List<DtcCatalogEntry> findAll() {
        return persistenceRepository.findAll().stream()
                .map(assembler::toDomain)
                .toList();
    }
}
