package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.CustomerAffiliationService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerAffiliationService.AffiliationLookup;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.AffiliateCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.CustomerResourceFromAggregateAssembler;
import com.andeva.atelier.platform.crm.interfaces.rest.transform.VehicleResourceFromAggregateAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Reception: tell walk-in customers apart from people who already use the platform (US44).
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer Affiliation", description = "Affiliate platform users as workshop customers")
public class CustomerAffiliationsController {

    private final CustomerAffiliationService affiliationService;

    public CustomerAffiliationsController(CustomerAffiliationService affiliationService) {
        this.affiliationService = Objects.requireNonNull(affiliationService, "affiliationService cannot be null");
    }

    @GetMapping("/affiliation-lookup")
    @PreAuthorize("hasAuthority('crm:customers:read')")
    @Operation(summary = "Look up an email: existing workshop customer, platform account and its vehicles")
    public ResponseEntity<?> lookup(@RequestParam String email, @AuthenticationPrincipal CustomUserDetails userDetails) {
        AffiliationLookup lookup = affiliationService.lookup(tenantOf(userDetails), email);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("workshopCustomer", lookup.workshopCustomer().map(CustomerResourceFromAggregateAssembler::toResourceFromEntity).orElse(null));
        body.put("platformAccount", lookup.platformAccount().orElse(null));
        body.put("vehicles", lookup.vehicles().stream().map(VehicleResourceFromAggregateAssembler::toResourceFromEntity).toList());
        body.put("nextStep", lookup.workshopCustomer().isPresent() ? "ALREADY_CUSTOMER"
                : lookup.platformAccount().isPresent() ? "AFFILIATE" : "REGISTER_WALK_IN");
        return ResponseEntity.ok(body);
    }

    @PostMapping("/affiliations")
    @PreAuthorize("hasAuthority('crm:customers:create')")
    @Operation(summary = "Affiliate a platform account as customer, linking the vehicles it already registered")
    public ResponseEntity<?> affiliate(@Valid @RequestBody AffiliateCustomerResource resource,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Result<Customer, ApplicationError> result = affiliationService.affiliate(
                tenantOf(userDetails), resource.email(), resource.taxId(), resource.phone());
        if (result.isFailure()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(result.getError());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResourceFromAggregateAssembler.toResourceFromEntity(result.getOrThrow()));
    }

    private static TenantId tenantOf(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new AccessDeniedException("Active tenant context is required");
        }
        return TenantId.of(userDetails.getTenantId());
    }
}
