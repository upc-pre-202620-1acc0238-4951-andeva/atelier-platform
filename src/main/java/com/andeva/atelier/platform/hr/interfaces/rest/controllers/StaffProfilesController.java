package com.andeva.atelier.platform.hr.interfaces.rest.controllers;

import com.andeva.atelier.platform.hr.application.commandservices.EmployeeProfileCommandService;
import com.andeva.atelier.platform.hr.application.queryservices.EmployeeProfileQueryService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.commands.AssignShiftToEmployeeCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RegisterEmployeeProfileCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateEmploymentStatusCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateSalaryCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.GetEmployeeProfileByMembershipIdQuery;
import com.andeva.atelier.platform.hr.domain.model.queries.ListEmployeeProfilesByBranchQuery;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.AssignShiftRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.RegisterEmployeeProfileRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateEmploymentStatusRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.UpdateSalaryRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.EmployeeProfileResource;
import com.andeva.atelier.platform.hr.interfaces.rest.transform.EmployeeProfileResourceAssembler;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hr/employees")
@Tag(name = "Staff Profiles", description = "Endpoints for employee employment profiles, shift assignments, salary and contract statuses")
public class StaffProfilesController {

    private final EmployeeProfileCommandService employeeProfileCommandService;
    private final EmployeeProfileQueryService employeeProfileQueryService;

    public StaffProfilesController(
            EmployeeProfileCommandService employeeProfileCommandService,
            EmployeeProfileQueryService employeeProfileQueryService
    ) {
        this.employeeProfileCommandService = Objects.requireNonNull(employeeProfileCommandService, "employeeProfileCommandService cannot be null");
        this.employeeProfileQueryService = Objects.requireNonNull(employeeProfileQueryService, "employeeProfileQueryService cannot be null");
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails, UUID tenantHeader) {
        if (tenantHeader != null) return TenantId.of(tenantHeader);
        if (userDetails != null && userDetails.getTenantId() != null) return TenantId.of(userDetails.getTenantId());
        return TenantId.of(UUID.randomUUID());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('iam:members:manage_roles') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Register and formalize an employee profile")
    public ResponseEntity<?> registerEmployeeProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @Valid @RequestBody RegisterEmployeeProfileRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        RegisterEmployeeProfileCommand command = EmployeeProfileResourceAssembler.toCommand(tenantId, request);
        Result<EmployeeProfile, ApplicationError> result = employeeProfileCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        EmployeeProfile created = result.getOrThrow();
        URI location = uriBuilder.path("/api/v1/hr/employees/{profileId}")
                .buildAndExpand(created.getId().value()).toUri();
        return ResponseEntity.created(location).body(EmployeeProfileResourceAssembler.toResource(created));
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasAuthority('iam:members:read') or hasRole('CHIEF_MECHANIC') or hasRole('WORKSHOP_ADMINISTRATOR') or isAuthenticated()")
    @Operation(summary = "Get employee profile by ID")
    public ResponseEntity<?> getEmployeeProfileById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID profileId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<EmployeeProfile> profileOpt = employeeProfileQueryService.handle(
                new GetEmployeeProfileByIdQuery(tenantId, EmployeeProfileId.of(profileId))
        );

        if (profileOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("No se encontró el expediente del colaborador")
            );
        }

        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResource(profileOpt.get()));
    }

    @GetMapping("/membership/{membershipId}")
    @PreAuthorize("hasAuthority('iam:members:read') or hasAuthority('hr:attendance:read_own') or hasRole('MECHANIC') or hasRole('CHIEF_MECHANIC') or isAuthenticated()")
    @Operation(summary = "Get employee profile by IAM membership ID")
    public ResponseEntity<?> getEmployeeProfileByMembershipId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID membershipId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        Optional<EmployeeProfile> profileOpt = employeeProfileQueryService.handle(
                new GetEmployeeProfileByMembershipIdQuery(tenantId, TenantMembershipId.of(membershipId))
        );

        if (profileOpt.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("No se encontró el expediente laboral vinculado a dicha membresía")
            );
        }

        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResource(profileOpt.get()));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('iam:members:read') or hasRole('CHIEF_MECHANIC') or hasRole('WORKSHOP_ADMINISTRATOR') or isAuthenticated()")
    @Operation(summary = "List all employee profiles assigned to a workshop branch")
    public ResponseEntity<List<EmployeeProfileResource>> getEmployeesByBranch(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID branchId
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        List<EmployeeProfile> list = employeeProfileQueryService.handle(
                new ListEmployeeProfilesByBranchQuery(tenantId, BranchId.of(branchId))
        );
        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResourceList(list));
    }

    @PutMapping("/{profileId}/shift")
    @PreAuthorize("hasAuthority('hr:shifts:manage') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Assign or update scheduled work shift for an employee")
    public ResponseEntity<?> assignShift(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID profileId,
            @Valid @RequestBody AssignShiftRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        AssignShiftToEmployeeCommand command = EmployeeProfileResourceAssembler.toCommand(tenantId, EmployeeProfileId.of(profileId), request);
        Result<EmployeeProfile, ApplicationError> result = employeeProfileCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResource(result.getOrThrow()));
    }

    @PutMapping("/{profileId}/salary")
    @PreAuthorize("hasAuthority('iam:members:compensate') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Update employee base salary and compensation scheme")
    public ResponseEntity<?> updateSalary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID profileId,
            @Valid @RequestBody UpdateSalaryRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        UpdateSalaryCommand command = EmployeeProfileResourceAssembler.toCommand(tenantId, EmployeeProfileId.of(profileId), request);
        Result<EmployeeProfile, ApplicationError> result = employeeProfileCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResource(result.getOrThrow()));
    }

    @PatchMapping("/{profileId}/status")
    @PreAuthorize("hasAuthority('iam:members:manage_roles') or hasRole('WORKSHOP_ADMINISTRATOR') or hasRole('WORKSHOP_OWNER') or isAuthenticated()")
    @Operation(summary = "Update employment status (ACTIVE, ON_LEAVE, TERMINATED)")
    public ResponseEntity<?> updateEmploymentStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @PathVariable UUID profileId,
            @Valid @RequestBody UpdateEmploymentStatusRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails, tenantHeader);
        UpdateEmploymentStatusCommand command = EmployeeProfileResourceAssembler.toCommand(tenantId, EmployeeProfileId.of(profileId), request);
        Result<EmployeeProfile, ApplicationError> result = employeeProfileCommandService.handle(command);

        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }

        return ResponseEntity.ok(EmployeeProfileResourceAssembler.toResource(result.getOrThrow()));
    }
}
