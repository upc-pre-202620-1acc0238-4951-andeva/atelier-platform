package com.andeva.atelier.platform.hr.domain.repositories;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository {
    AttendanceRecord save(AttendanceRecord attendanceRecord);
    Optional<AttendanceRecord> findById(AttendanceRecordId id);
    Optional<AttendanceRecord> findActiveByMembership(TenantId tenantId, TenantMembershipId membershipId);
    List<AttendanceRecord> findAllByBranchIdAndDate(TenantId tenantId, BranchId branchId, LocalDate date);
    List<AttendanceRecord> findAllByMembershipAndPeriod(TenantId tenantId, TenantMembershipId membershipId, Instant start, Instant end);
    boolean hasActiveClockIn(TenantId tenantId, TenantMembershipId membershipId);
}
