package com.andeva.atelier.platform.hr.interfaces.rest.controllers;

import com.andeva.atelier.platform.hr.application.commandservices.AttendanceCommandService;
import com.andeva.atelier.platform.hr.application.queryservices.AttendanceQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.commands.JustifyAttendanceCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockInCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockOutCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeAttendanceHistoryQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetTodayAttendanceByMembershipQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListAttendanceByBranchAndDateQuery;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.ClockInRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.ClockOutRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.JustifyAttendanceRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.AttendanceResource;
import com.andeva.atelier.platform.hr.interfaces.rest.transform.AttendanceResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/attendances")
@Tag(name = "Attendance", description = "Endpoints for physical attendance clock-in/out and geofenced verification")
public class AttendanceController {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    public AttendanceController(
            AttendanceCommandService attendanceCommandService,
            AttendanceQueryService attendanceQueryService
    ) {
        this.attendanceCommandService = Objects.requireNonNull(attendanceCommandService, "attendanceCommandService cannot be null");
        this.attendanceQueryService = Objects.requireNonNull(attendanceQueryService, "attendanceQueryService cannot be null");
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID tenantHeader) {
        if (tenantHeader != null) return TenantId.of(tenantHeader);
        if (userDetails != null && userDetails.getTenantId() != null) return TenantId.of(userDetails.getTenantId());
        return TenantId.of(UUID.randomUUID());
    }

    private TenantMembershipId resolveMembershipId(CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUserId() != null) {
            return TenantMembershipId.of(userDetails.getUserId());
        }
        return TenantMembershipId.of(UUID.randomUUID());
    }

    @PostMapping("/clock-in")
    @PreAuthorize("hasAuthority('hr:attendance:clock_in') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or isAuthenticated()")
    @Operation(summary = "Record physical clock-in with geofenced location")
    public ResponseEntity<?> clockIn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody ClockInRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        TenantMembershipId membershipId = resolveMembershipId(userDetails);

        RecordClockInCommand command = AttendanceResourceAssembler.toCommand(tenantId, membershipId, request);
        Result<AttendanceRecord, ApplicationError> result = attendanceCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        AttendanceRecord record = result.getOrThrow();
        URI location = uriBuilder.path("/api/v1/hr/attendances/{attendanceId}")
                .buildAndExpand(record.getId().value()).toUri();
        return ResponseEntity.created(location).body(AttendanceResourceAssembler.toResource(record));
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasAuthority('hr:attendance:clock_out') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or isAuthenticated()")
    @Operation(summary = "Record physical clock-out")
    public ResponseEntity<?> clockOut(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @RequestBody(required = false) ClockOutRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        TenantMembershipId membershipId = resolveMembershipId(userDetails);

        RecordClockOutCommand command = AttendanceResourceAssembler.toCommand(tenantId, membershipId, null, request);
        Result<AttendanceRecord, ApplicationError> result = attendanceCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(AttendanceResourceAssembler.toResource(result.getOrThrow()));
    }

    @PostMapping("/{attendanceId}/justify")
    @PreAuthorize("hasAuthority('hr:justifications:approve') or hasRole('CHIEF_MECHANIC') or hasRole('WORKSHOP_ADMINISTRATOR') or isAuthenticated()")
    @Operation(summary = "Justify a late or absent attendance record")
    public ResponseEntity<?> justifyAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID attendanceId,
            @Valid @RequestBody JustifyAttendanceRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        TenantMembershipId supervisorId = resolveMembershipId(userDetails);

        JustifyAttendanceCommand command = AttendanceResourceAssembler.toCommand(
                tenantId, AttendanceRecordId.of(attendanceId), supervisorId, request
        );
        Result<AttendanceRecord, ApplicationError> result = attendanceCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.ok(AttendanceResourceAssembler.toResource(result.getOrThrow()));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('hr:attendance:audit_all') or hasRole('CHIEF_MECHANIC') or hasRole('WORKSHOP_ADMINISTRATOR') or isAuthenticated()")
    @Operation(summary = "List attendance records for a specific branch and date")
    public ResponseEntity<List<AttendanceResource>> getAttendanceByBranch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<AttendanceRecord> list = attendanceQueryService.handle(
                new ListAttendanceByBranchAndDateQuery(tenantId, BranchId.of(branchId), date)
        );
        return ResponseEntity.ok(AttendanceResourceAssembler.toResourceList(list));
    }

    @GetMapping("/employee/{membershipId}/history")
    @PreAuthorize("hasAnyAuthority('hr:attendance:audit_all', 'hr:attendance:read_own') or isAuthenticated()")
    @Operation(summary = "Get employee attendance history within a date range")
    public ResponseEntity<List<AttendanceResource>> getEmployeeAttendanceHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID membershipId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<AttendanceRecord> list = attendanceQueryService.handle(
                new GetEmployeeAttendanceHistoryQuery(tenantId, TenantMembershipId.of(membershipId), startDate, endDate)
        );
        return ResponseEntity.ok(AttendanceResourceAssembler.toResourceList(list));
    }

    @GetMapping("/employee/{membershipId}/active")
    @PreAuthorize("hasAnyAuthority('hr:attendance:audit_all', 'hr:attendance:read_own') or isAuthenticated()")
    @Operation(summary = "Get active clock-in status for an employee today")
    public ResponseEntity<?> getActiveAttendance(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID membershipId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<AttendanceRecord> recordOpt = attendanceQueryService.handle(
                new GetTodayAttendanceByMembershipQuery(tenantId, TenantMembershipId.of(membershipId))
        );
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AttendanceResourceAssembler.toResource(recordOpt.get()));
    }
}
