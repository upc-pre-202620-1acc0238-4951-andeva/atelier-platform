package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers.TenantPersistenceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.repositories.TenantPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Repository adapter implementing {@link TenantRepository} using Spring Data JPA.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantPersistenceRepository persistenceRepository;

    public TenantRepositoryImpl(TenantPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        Objects.requireNonNull(tenant, "Tenant cannot be null");
        TenantPersistenceEntity entity = TenantPersistenceAssembler.toEntity(tenant);
        TenantPersistenceEntity saved = persistenceRepository.save(entity);
        return TenantPersistenceAssembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findById(TenantId id) {
        if (id == null) {
            return Optional.empty();
        }
        return persistenceRepository.findById(id.value())
                .map(TenantPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findByTaxId(TaxId taxId) {
        if (taxId == null) {
            return Optional.empty();
        }
        return persistenceRepository.findByTaxId(taxId.value())
                .map(TenantPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTaxId(TaxId taxId) {
        if (taxId == null) {
            return false;
        }
        return persistenceRepository.existsByTaxId(taxId.value());
    }
}
