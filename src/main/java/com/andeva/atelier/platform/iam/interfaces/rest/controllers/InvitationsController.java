package com.andeva.atelier.platform.iam.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.application.commandservices.InvitationCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.dto.InvitationValidationResult;
import com.andeva.atelier.platform.iam.application.queryservices.InvitationQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.commands.AcceptInvitationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.InviteStaffCommand;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRoleByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.ValidateInvitationTokenQuery;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.AcceptInvitationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.InviteStaffResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.AuthenticatedUserResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.InvitationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.InvitationValidationResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.TenantSummaryResource;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.InvitationResourceFromAggregateAssembler;
import com.andeva.atelier.platform.iam.interfaces.rest.transform.TenantResourceFromAggregateAssembler;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller managing staff onboarding invitations, cryptographic token validation,
 * and invitation acceptance.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Staff Invitations", description = "Endpoints for issuing, validating, and accepting staff onboarding invitations")
public class InvitationsController {

    private final InvitationCommandService invitationCommandService;
    private final InvitationQueryService invitationQueryService;
    private final TenantQueryService tenantQueryService;
    private final RoleQueryService roleQueryService;

    public InvitationsController(
            InvitationCommandService invitationCommandService,
            InvitationQueryService invitationQueryService,
            TenantQueryService tenantQueryService,
            RoleQueryService roleQueryService
    ) {
        this.invitationCommandService = Objects.requireNonNull(invitationCommandService, "InvitationCommandService cannot be null");
        this.invitationQueryService = Objects.requireNonNull(invitationQueryService, "InvitationQueryService cannot be null");
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
        this.roleQueryService = Objects.requireNonNull(roleQueryService, "RoleQueryService cannot be null");
    }

    /**
     * Issues and dispatches a cryptographic staff invitation to an prospective collaborator via Resend email.
     */
    @Operation(summary = "Invite staff member by email to workshop tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff invitation created and dispatched"),
            @ApiResponse(responseCode = "400", description = "Invalid email format or missing role"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Missing authority iam:members:invite or subscription quota exceeded"),
            @ApiResponse(responseCode = "409", description = "Invitee already has active membership or pending invitation")
    })
    @PostMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('iam:members:invite')")
    public ResponseEntity<?> inviteStaff(
            @PathVariable UUID tenantId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InviteStaffResource resource
    ) {
        if (!isAuthorizedForTenant(tenantId, userDetails)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.forbidden("Access denied: Cross-tenant access prohibited"));
        }

        InviteStaffCommand command = new InviteStaffCommand(
                TenantId.of(tenantId),
                EmailAddress.of(resource.email()),
                RoleId.of(resource.roleId()),
                java.time.Duration.ofDays(7)
        );
        Result<Invitation, ApplicationError> result = invitationCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                InvitationResourceFromAggregateAssembler::toResourceFromAggregate,
                HttpStatus.CREATED
        );
    }

    /**
     * Validates cryptographic invitation token for rendering the client onboarding view.
     */
    @Operation(summary = "Validate cryptographic invitation token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is authentic, active, and redeemable"),
            @ApiResponse(responseCode = "400", description = "Missing or empty token parameter"),
            @ApiResponse(responseCode = "404", description = "Invitation token does not exist or was revoked"),
            @ApiResponse(responseCode = "410", description = "Invitation token has expired past its 7-day validity")
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateInvitationToken(@RequestParam String token) {
        if (token == null || token.isBlank()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.badRequest("Token parameter cannot be null or empty"));
        }

        Optional<InvitationValidationResult> validationOptional = invitationQueryService.handle(
                new ValidateInvitationTokenQuery(token.trim())
        );
        if (validationOptional.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Invitation token not found"));
        }

        InvitationValidationResult validation = validationOptional.get();
        if (!validation.valid()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.badRequest(validation.failureReason() != null
                            ? validation.failureReason()
                            : "Invitation has expired past its validity window"));
        }

        Invitation invitation = validation.invitation();
        String roleName = roleQueryService.handle(new GetRoleByIdQuery(invitation.targetRoleId()))
                .map(Role::name)
                .orElse("Member");

        InvitationValidationResource response = new InvitationValidationResource(
                true,
                invitation.email().value(),
                validation.tenantName(),
                roleName
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Accepts a staff invitation, provisions employee credentials and membership,
     * and issues an immediate JWT Bearer authentication token.
     */
    @Operation(summary = "Accept invitation and register collaborator account credentials")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation accepted, account provisioned, session issued"),
            @ApiResponse(responseCode = "400", description = "Validation constraint failure on password or names"),
            @ApiResponse(responseCode = "404", description = "Invitation token not found"),
            @ApiResponse(responseCode = "409", description = "Invitation already accepted previously"),
            @ApiResponse(responseCode = "410", description = "Invitation token expired")
    })
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@Valid @RequestBody AcceptInvitationResource resource) {
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                resource.token(),
                resource.password(),
                resource.firstName(),
                resource.lastName(),
                resource.phone()
        );
        Result<AuthenticatedUser, ApplicationError> result = invitationCommandService.handle(command);

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                this::toAuthenticatedUserResource,
                HttpStatus.CREATED
        );
    }

    private AuthenticatedUserResource toAuthenticatedUserResource(AuthenticatedUser authUser) {
        TenantSummaryResource tenantSummary = null;
        if (authUser.tenantId() != null) {
            tenantSummary = tenantQueryService.handle(new GetTenantByIdQuery(authUser.tenantId()))
                    .map(TenantResourceFromAggregateAssembler::toSummaryFromAggregate)
                    .orElse(new TenantSummaryResource(authUser.tenantId().value(), "", ""));
        }

        String fullName = (authUser.user().profile() != null)
                ? authUser.user().profile().getFullName()
                : "";

        return new AuthenticatedUserResource(
                authUser.user().id().value(),
                authUser.user().email().value(),
                fullName,
                authUser.token(),
                "Bearer",
                tenantSummary,
                authUser.permissions().stream().sorted().toList()
        );
    }

    private boolean isAuthorizedForTenant(UUID tenantId, CustomUserDetails userDetails) {
        return userDetails != null && userDetails.getTenantId() != null && userDetails.getTenantId().equals(tenantId);
    }
}
