package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers.VoucherPaymentPersistenceAssembler;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.ElectronicVoucherPersistenceRepository;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.repositories.VoucherPaymentPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
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
 * Spring Data JPA Adapter implementing the {@link VoucherPaymentRepository} domain port.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional
public class VoucherPaymentRepositoryImpl implements VoucherPaymentRepository {

    private final VoucherPaymentPersistenceRepository jpaRepository;
    private final ElectronicVoucherPersistenceRepository voucherJpaRepository;
    private final VoucherPaymentPersistenceAssembler assembler;

    public VoucherPaymentRepositoryImpl(
            VoucherPaymentPersistenceRepository jpaRepository,
            ElectronicVoucherPersistenceRepository voucherJpaRepository,
            VoucherPaymentPersistenceAssembler assembler
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "JPA repository cannot be null");
        this.voucherJpaRepository = Objects.requireNonNull(voucherJpaRepository, "Voucher JPA repository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "Assembler cannot be null");
    }

    @Override
    public VoucherPayment save(VoucherPayment payment) {
        Objects.requireNonNull(payment, "VoucherPayment cannot be null");

        ElectronicVoucherPersistenceEntity voucherEntity = voucherJpaRepository.findById(payment.getVoucherId().value())
                .orElse(null);

        VoucherPaymentPersistenceEntity entity = assembler.toEntity(payment, voucherEntity);
        VoucherPaymentPersistenceEntity saved = jpaRepository.save(entity);
        return assembler.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VoucherPayment> findById(PaymentId id) {
        Objects.requireNonNull(id, "PaymentId cannot be null");
        return jpaRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherPayment> findAllByVoucherId(VoucherId voucherId) {
        Objects.requireNonNull(voucherId, "VoucherId cannot be null");
        return jpaRepository.findAllByVoucherId(voucherId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherPayment> findAllByBranchIdAndDate(BranchId branchId, LocalDate date) {
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        LocalDate target = date != null ? date : LocalDate.now();
        Instant dayStart = target.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant dayEnd = target.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        return jpaRepository.findAllByBranchAndDate(branchId.value(), dayStart, dayEnd).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherPayment> findByBranchIdAndDateRange(BranchId branchId, Instant from, Instant to) {
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Instant start = from != null ? from : LocalDate.now().minusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to != null ? to : Instant.now();

        return jpaRepository.findAllByBranchAndDate(branchId.value(), start, end).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherPayment> findByTenantIdAndDateRange(com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId tenantId, Instant from, Instant to) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Instant start = from != null ? from : LocalDate.now().minusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = to != null ? to : Instant.now();

        return jpaRepository.findByTenantAndDateRange(tenantId.value(), start, end).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
