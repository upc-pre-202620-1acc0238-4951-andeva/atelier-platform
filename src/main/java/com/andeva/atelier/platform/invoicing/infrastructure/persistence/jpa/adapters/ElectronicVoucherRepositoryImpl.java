package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.ElectronicVoucherPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.ElectronicVoucherPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA Adapter implementing the {@link ElectronicVoucherRepository} domain port.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class ElectronicVoucherRepositoryImpl implements ElectronicVoucherRepository {

    private final ElectronicVoucherPersistenceRepository jpaRepository;
    private final ElectronicVoucherPersistenceAssembler assembler;

    public ElectronicVoucherRepositoryImpl(
            ElectronicVoucherPersistenceRepository jpaRepository,
            ElectronicVoucherPersistenceAssembler assembler
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "JPA repository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "Assembler cannot be null");
    }

    @Override
    public ElectronicVoucher save(ElectronicVoucher voucher) {
        Objects.requireNonNull(voucher, "ElectronicVoucher cannot be null");
        ElectronicVoucherPersistenceEntity entity = assembler.toEntity(voucher);
        ElectronicVoucherPersistenceEntity saved = jpaRepository.save(entity);
        return assembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ElectronicVoucher> findById(VoucherId id) {
        Objects.requireNonNull(id, "VoucherId cannot be null");
        return jpaRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ElectronicVoucher> findByTenantIdAndSerieAndNumber(
            TenantId tenantId,
            VoucherSerie serie,
            VoucherNumber number
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(serie, "VoucherSerie cannot be null");
        Objects.requireNonNull(number, "VoucherNumber cannot be null");

        return jpaRepository.findByTenantIdAndSerieAndNumber(tenantId.value(), serie.value(), number.value())
                .map(assembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ElectronicVoucher> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Instant from = LocalDate.now().minusYears(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = Instant.now();
        return jpaRepository.findAllByTenantAndDateRange(tenantId.value(), from, to).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ElectronicVoucher> findAllByTenantIdAndDateRange(TenantId tenantId, LocalDate from, LocalDate to) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Instant start = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : LocalDate.now().minusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to != null ? to.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC) : Instant.now();

        return jpaRepository.findAllByTenantAndDateRange(tenantId.value(), start, end).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ElectronicVoucher> findAllByWorkOrderId(WorkOrderId workOrderId) {
        Objects.requireNonNull(workOrderId, "WorkOrderId cannot be null");
        return jpaRepository.findAllByWorkOrderId(workOrderId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantIdAndSerieAndNumber(TenantId tenantId, VoucherSerie serie, VoucherNumber number) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(serie, "VoucherSerie cannot be null");
        Objects.requireNonNull(number, "VoucherNumber cannot be null");

        return jpaRepository.existsByTenantIdAndSerieAndNumber(tenantId.value(), serie.value(), number.value());
    }
}
