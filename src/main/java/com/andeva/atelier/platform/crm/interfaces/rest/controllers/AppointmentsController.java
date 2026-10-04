package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.AppointmentQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByCustomerQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByTenantAndBranchQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByVehicleQuery;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CancelAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.RescheduleAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.ScheduleAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.AppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.AppointmentResourceFromAggregateAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.CancelAppointmentCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.RescheduleAppointmentCommandFromResourceAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.ScheduleAppointmentCommandFromResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller orchestrating service appointment scheduling, status transitions, and agenda projections.
 * Canonical specification from 03-crm-and-fleet.md Section 5.3.1.
 *
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Endpoints for scheduling, confirming, rescheduling, and tracking service appointments")
public class AppointmentsController {

    private final AppointmentCommandService appointmentCommandService;
    private final AppointmentQueryService appointmentQueryService;

    public AppointmentsController(
            AppointmentCommandService appointmentCommandService,
            AppointmentQueryService appointmentQueryService
    ) {
        this.appointmentCommandService = Objects.requireNonNull(appointmentCommandService, "AppointmentCommandService cannot be null");
        this.appointmentQueryService = Objects.requireNonNull(appointmentQueryService, "AppointmentQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:appointments:create')")
    @Operation(summary = "Schedule a service appointment")
    public ResponseEntity<?> scheduleAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScheduleAppointmentResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required to schedule appointment"));
        }

        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                ScheduleAppointmentCommandFromResourceAssembler.toCommandFromResource(userDetails.getTenantId(), resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('crm:appointments:read')")
    @Operation(summary = "Retrieve appointment details by identifier")
    public ResponseEntity<?> getAppointmentById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Optional<Appointment> appointmentOpt = appointmentQueryService.handle(
                new GetAppointmentByIdQuery(TenantId.of(userDetails.getTenantId()), AppointmentId.of(id)));

        if (appointmentOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Appointment", id));
        }

        return ResponseEntity.ok(AppointmentResourceFromAggregateAssembler.toResourceFromEntity(appointmentOpt.get()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('crm:appointments:read')")
    @Operation(summary = "Query appointments for branch and date agenda")
    public ResponseEntity<?> getAppointments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        LocalDate queryDate = date != null ? date : LocalDate.now();

        AppointmentStatus appointmentStatus = status != null && !status.isBlank()
                ? AppointmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT))
                : null;

        List<Appointment> appointments = appointmentQueryService.handle(
                new GetAppointmentsByTenantAndBranchQuery(
                        TenantId.of(userDetails.getTenantId()),
                        BranchId.of(branchId),
                        queryDate,
                        appointmentStatus
                ));

        List<AppointmentResource> resources = appointments.stream()
                .map(AppointmentResourceFromAggregateAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('crm:appointments:update')")
    @Operation(summary = "Confirm pending appointment")
    public ResponseEntity<?> confirmAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                new ConfirmAppointmentCommand(TenantId.of(userDetails.getTenantId()), AppointmentId.of(id)));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/arrive")
    @PreAuthorize("hasAuthority('crm:appointments:update')")
    @Operation(summary = "Mark appointment as arrived at the workshop bay")
    public ResponseEntity<?> markArrived(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                new MarkAppointmentArrivedCommand(TenantId.of(userDetails.getTenantId()), AppointmentId.of(id)));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAuthority('crm:appointments:update')")
    @Operation(summary = "Reschedule an existing appointment to a new date and time")
    public ResponseEntity<?> rescheduleAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                RescheduleAppointmentCommandFromResourceAssembler.toCommandFromResource(
                        userDetails.getTenantId(), id, resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('crm:appointments:update')")
    @Operation(summary = "Cancel an appointment recording a justification")
    public ResponseEntity<?> cancelAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody CancelAppointmentResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Appointment, ApplicationError> result = appointmentCommandService.handle(
                CancelAppointmentCommandFromResourceAssembler.toCommandFromResource(
                        userDetails.getTenantId(), id, resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                AppointmentResourceFromAggregateAssembler::toResourceFromEntity,
                HttpStatus.OK
        );
    }
}
