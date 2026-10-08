package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerMembershipCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerMembershipQueryService;
import com.andeva.atelier.platform.crm.domain.model.commands.RevokeCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembersByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembershipsByUserIdQuery;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.InviteCustomerMemberResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.CustomerMembershipResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.CustomerMembershipResourceFromEntityAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.InviteCustomerMemberCommandFromResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller managing corporate fleet delegations and customer membership assignments.
 * Canonical specification from 02-crm-and-fleet.md Section 5.
 *
 * @author Joel Huamani Estefanero
 * @author Adiel Sanchez Santin
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}/memberships")
@Tag(name = "Customer Memberships", description = "Endpoints for managing corporate customer fleet memberships and roles")
public class CustomerMembershipsController {

    private final CustomerMembershipCommandService customerMembershipCommandService;
    private final CustomerMembershipQueryService customerMembershipQueryService;

    public CustomerMembershipsController(
            CustomerMembershipCommandService customerMembershipCommandService,
            CustomerMembershipQueryService customerMembershipQueryService
    ) {
        this.customerMembershipCommandService = Objects.requireNonNull(customerMembershipCommandService, "CustomerMembershipCommandService cannot be null");
        this.customerMembershipQueryService = Objects.requireNonNull(customerMembershipQueryService, "CustomerMembershipQueryService cannot be null");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('crm:customers:manage') or hasAuthority('crm:fleets:manage')")
    @Operation(summary = "Invite or assign user to corporate customer fleet")
    public ResponseEntity<?> inviteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID customerId,
            @Valid @RequestBody InviteCustomerMemberResource resource
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<CustomerMembership, ApplicationError> result = customerMembershipCommandService.handle(
                InviteCustomerMemberCommandFromResourceAssembler.toCommandFromResource(
                        userDetails.getTenantId(), customerId, resource));

        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                CustomerMembershipResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('crm:customers:manage') or hasAuthority('crm:fleets:manage')")
    @Operation(summary = "Revoke corporate fleet membership from user")
    public ResponseEntity<?> revokeMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID customerId,
            @PathVariable UUID userId
    ) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.unauthorized("Authentication required"));
        }

        Result<Void, ApplicationError> result = customerMembershipCommandService.handle(
                new RevokeCustomerMemberCommand(
                        TenantId.of(userDetails.getTenantId()),
                        CustomerId.of(customerId),
                        UserId.of(userId)
                ));

        if (result instanceof Result.Failure<Void, ApplicationError> failure) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('crm:customers:read') or hasAuthority('crm:fleets:manage')")
    @Operation(summary = "List corporate fleet members assigned to customer")
    public ResponseEntity<?> getMembersByCustomerId(@PathVariable UUID customerId) {
        List<CustomerMembership> members = customerMembershipQueryService.handle(
                new GetCustomerMembersByCustomerIdQuery(CustomerId.of(customerId)));

        List<CustomerMembershipResource> resources = members.stream()
                .map(CustomerMembershipResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(resources);
    }
}
