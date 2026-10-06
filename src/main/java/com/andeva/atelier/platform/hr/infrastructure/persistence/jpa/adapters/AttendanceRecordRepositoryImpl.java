package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers.AttendanceRecordPersistenceAssembler;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.AttendanceRecordPersistenceEntity;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories.AttendanceRecordPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AttendanceRecordRepositoryImpl implements AttendanceRecordRepository {

    private final AttendanceRecordPersistenceRepository persistenceRepository;
    private final AttendanceRecordPersistenceAssembler assembler;
    private final ApplicationEventPublisher eventPublisher;

    public AttendanceRecordRepositoryImpl(
            AttendanceRecordPersistenceRepository persistenceRepository,
            AttendanceRecordPersistenceAssembler assembler,
            ApplicationEventPublisher eventPublisher
    ) {
        this.persistenceRepository = Objects.requireNonNull(persistenceRepository, "persistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "assembler cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<AttendanceRecord> findById(AttendanceRecordId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return persistenceRepository.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<AttendanceRecord> findActiveByMembership(TenantId tenantId, TenantMembershipId membershipId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        List<AttendanceRecordPersistenceEntity> openClockIns =
                persistenceRepository.findOpenClockIns(tenantId.value(), membershipId.value());
        return openClockIns.stream().findFirst().map(assembler::toDomain);
    }

    @Override
    public List<AttendanceRecord> findAllByBranchIdAndDate(TenantId tenantId, BranchId branchId, LocalDate date) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        LocalDate targetDate = date != null ? date : LocalDate.now();
        Instant dayStart = targetDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant dayEnd = targetDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        return persistenceRepository.findAllByBranchAndDay(tenantId.value(), branchId.value(), dayStart, dayEnd)
                .stream()
                .map(assembler::toDomain)
                .toList();
    }

    @Override
    public List<AttendanceRecord> findAllByMembershipAndPeriod(
            TenantId tenantId,
            TenantMembershipId membershipId,
            Instant start,
            Instant end
    ) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        return persistenceRepository.findAllByMembershipAndPeriod(
                tenantId.value(), membershipId.value(), start, end
        ).stream().map(assembler::toDomain).toList();
    }

    @Override
    public boolean hasActiveClockIn(TenantId tenantId, TenantMembershipId membershipId) {
        return findActiveByMembership(tenantId, membershipId).isPresent();
    }

    @Override
    public AttendanceRecord save(AttendanceRecord attendanceRecord) {
        Objects.requireNonNull(attendanceRecord, "attendanceRecord cannot be null");
        AttendanceRecordPersistenceEntity entity = persistenceRepository.findById(attendanceRecord.getId().value())
                .map(existing -> {
                    assembler.updateEntity(existing, attendanceRecord);
                    return existing;
                })
                .orElseGet(() -> assembler.toEntity(attendanceRecord));

        AttendanceRecordPersistenceEntity saved = persistenceRepository.save(entity);
        AttendanceRecord domain = assembler.toDomain(saved);

        attendanceRecord.domainEvents().forEach(eventPublisher::publishEvent);
        attendanceRecord.clearDomainEvents();

        return domain;
    }
}
