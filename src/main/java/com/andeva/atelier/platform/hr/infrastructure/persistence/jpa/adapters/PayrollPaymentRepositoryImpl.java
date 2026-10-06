package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.hr.domain.repositories.PayrollPaymentRepository;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers.PayrollPaymentPersistenceAssembler;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.PayrollPaymentPersistenceEntity;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories.PayrollPaymentPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PayrollPaymentRepositoryImpl implements PayrollPaymentRepository {

    private final PayrollPaymentPersistenceRepository persistenceRepository;
    private final PayrollPaymentPersistenceAssembler assembler;
    private final ApplicationEventPublisher eventPublisher;

    public PayrollPaymentRepositoryImpl(
            PayrollPaymentPersistenceRepository persistenceRepository,
            PayrollPaymentPersistenceAssembler assembler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "assembler cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<PayrollPayment> findById(PayrollPaymentId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<PayrollPayment> findByMembershipAndPeriod(TenantId tenantId, TenantMembershipId membershipId, PayPeriod period) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(period, "period cannot be null");
        return persistenceRepository.findByTenantIdAndMembershipIdAndPeriodStartAndPeriodEnd(
                tenantId.value(), membershipId.value(), period.startDate(), period.endDate()
        ).map(assembler::toDomain);
    }

    @Override
    public List<PayrollPayment> findAllByTenantIdAndPeriod(TenantId tenantId, LocalDate start, LocalDate end) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantIdAndPeriod(tenantId.value(), start, end).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<PayrollPayment> findAllByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return persistenceRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<PayrollPayment> findAllByMembershipId(TenantId tenantId, TenantMembershipId membershipId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        return persistenceRepository.findAllByTenantIdAndMembershipIdOrderByPeriodStartDesc(
                tenantId.value(), membershipId.value()
        ).stream().map(assembler::toDomain).toList();
    }

    @Override
    public PayrollPayment save(PayrollPayment payrollPayment) {
        Objects.requireNonNull(payrollPayment, "payrollPayment cannot be null");
        PayrollPaymentPersistenceEntity entity = persistenceRepository.findById(payrollPayment.getId().value())
                .map(existing -> {
                    assembler.updateEntity(existing, payrollPayment);
                    return existing;
                })
                .orElseGet(() -> assembler.toEntity(payrollPayment));

        PayrollPaymentPersistenceEntity saved = persistenceRepository.save(entity);
        PayrollPayment domain = assembler.toDomain(saved);

        payrollPayment.domainEvents().forEach(eventPublisher::publishEvent);
        payrollPayment.clearDomainEvents();

        return domain;
    }
}
