package com.andeva.atelier.platform.hr.application.internal.queryservices;

import com.andeva.atelier.platform.hr.application.queryservices.AttendanceQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.queries.GetAttendanceRecordByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeAttendanceHistoryQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetTodayAttendanceByMembershipQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListAttendanceByBranchAndDateQuery;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AttendanceQueryServiceImpl implements AttendanceQueryService {

    private final AttendanceRecordRepository attendanceRepository;

    public AttendanceQueryServiceImpl(AttendanceRecordRepository attendanceRepository) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository cannot be null");
    }

    @Override
    public Optional<AttendanceRecord> handle(GetAttendanceRecordByIdQuery query) {
        return attendanceRepository.findById(query.attendanceRecordId())
                .filter(a -> a.getTenantId().equals(query.tenantId()));
    }

    @Override
    public List<AttendanceRecord> handle(ListAttendanceByBranchAndDateQuery query) {
        return attendanceRepository.findAllByBranchIdAndDate(query.tenantId(), query.branchId(), query.date());
    }

    @Override
    public List<AttendanceRecord> handle(GetEmployeeAttendanceHistoryQuery query) {
        Instant start = query.startDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = query.endDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return attendanceRepository.findAllByMembershipAndPeriod(query.tenantId(), query.membershipId(), start, end);
    }

    @Override
    public Optional<AttendanceRecord> handle(GetTodayAttendanceByMembershipQuery query) {
        return attendanceRepository.findActiveByMembership(query.tenantId(), query.membershipId());
    }
}
