package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.AttendanceRecordPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordPersistenceRepository extends JpaRepository<AttendanceRecordPersistenceEntity, UUID> {

    @Query("SELECT a FROM AttendanceRecordPersistenceEntity a WHERE a.tenantId = :tenantId AND a.membershipId = :membershipId AND a.clockOut IS NULL ORDER BY a.clockIn DESC")
    List<AttendanceRecordPersistenceEntity> findOpenClockIns(
            @Param("tenantId") UUID tenantId,
            @Param("membershipId") UUID membershipId
    );

    @Query("SELECT a FROM AttendanceRecordPersistenceEntity a WHERE a.tenantId = :tenantId AND a.branchId = :branchId AND a.clockIn >= :dayStart AND a.clockIn < :dayEnd ORDER BY a.clockIn DESC")
    List<AttendanceRecordPersistenceEntity> findAllByBranchAndDay(
            @Param("tenantId") UUID tenantId,
            @Param("branchId") UUID branchId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );

    @Query("SELECT a FROM AttendanceRecordPersistenceEntity a WHERE a.tenantId = :tenantId AND a.membershipId = :membershipId AND a.clockIn >= :start AND a.clockIn <= :end ORDER BY a.clockIn ASC")
    List<AttendanceRecordPersistenceEntity> findAllByMembershipAndPeriod(
            @Param("tenantId") UUID tenantId,
            @Param("membershipId") UUID membershipId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
