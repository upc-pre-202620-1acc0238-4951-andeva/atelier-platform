package com.andeva.atelier.platform.hr.application.queryservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.queries.GetAttendanceRecordByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeAttendanceHistoryQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetTodayAttendanceByMembershipQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListAttendanceByBranchAndDateQuery;

import java.util.List;
import java.util.Optional;

public interface AttendanceQueryService {
    Optional<AttendanceRecord> handle(GetAttendanceRecordByIdQuery query);
    List<AttendanceRecord> handle(ListAttendanceByBranchAndDateQuery query);
    List<AttendanceRecord> handle(GetEmployeeAttendanceHistoryQuery query);
    Optional<AttendanceRecord> handle(GetTodayAttendanceByMembershipQuery query);
}
