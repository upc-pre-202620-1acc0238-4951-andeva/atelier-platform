package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.SeriesConfigurationPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.SeriesConfigurationPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.SeriesConfigurationPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA Adapter implementing the {@link SeriesConfigurationRepository} domain port,
 * providing pessimistic write locking to prevent correlative race conditions.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class SeriesConfigurationRepositoryImpl implements SeriesConfigurationRepository {

    private final SeriesConfigurationPersistenceRepository jpaRepository;
    private final SeriesConfigurationPersistenceAssembler assembler;

    public SeriesConfigurationRepositoryImpl(
            SeriesConfigurationPersistenceRepository jpaRepository,
            SeriesConfigurationPersistenceAssembler assembler
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "JPA repository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "Assembler cannot be null");
    }

    @Override
    public SeriesConfiguration save(SeriesConfiguration seriesConfig) {
        Objects.requireNonNull(seriesConfig, "SeriesConfiguration cannot be null");
        SeriesConfigurationPersistenceEntity entity = assembler.toEntity(seriesConfig);
        SeriesConfigurationPersistenceEntity saved = jpaRepository.save(entity);
        return assembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SeriesConfiguration> findById(SeriesConfigurationId id) {
        Objects.requireNonNull(id, "SeriesConfigurationId cannot be null");
        return jpaRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<SeriesConfiguration> findByTenantIdAndBranchIdAndVoucherTypeAndActive(
            TenantId tenantId,
            BranchId branchId,
            VoucherType type
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(type, "VoucherType cannot be null");

        // Uses pessimistic write lock to ensure zero correlative collisions
        return jpaRepository.findActiveForUpdate(tenantId.value(), branchId.value(), type.name())
                .map(assembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SeriesConfiguration> findByTenantIdAndBranchIdAndVoucherTypeAndSerie(
            TenantId tenantId,
            BranchId branchId,
            VoucherType type,
            VoucherSerie serie
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(type, "VoucherType cannot be null");
        Objects.requireNonNull(serie, "VoucherSerie cannot be null");

        return jpaRepository.findByTenantIdAndBranchIdAndVoucherTypeAndSerie(
                tenantId.value(),
                branchId.value(),
                type.name(),
                serie.value()
        ).map(assembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeriesConfiguration> findAllByTenantIdAndBranchId(TenantId tenantId, BranchId branchId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");

        return jpaRepository.findAllByTenantIdAndBranchId(tenantId.value(), branchId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantIdAndBranchIdAndSerie(TenantId tenantId, BranchId branchId, VoucherSerie serie) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(serie, "VoucherSerie cannot be null");

        return jpaRepository.existsByTenantIdAndBranchIdAndSerie(tenantId.value(), branchId.value(), serie.value());
    }
}
