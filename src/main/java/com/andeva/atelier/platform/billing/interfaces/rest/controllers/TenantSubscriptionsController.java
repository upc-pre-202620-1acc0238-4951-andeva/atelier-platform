package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.application.queryservices.TenantSubscriptionQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.InitiateCheckoutSessionCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetTenantSubscriptionQuery;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CancelSubscriptionRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CreateCheckoutSessionRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CustomerPortalRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.CheckoutSessionResponse;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.CustomerPortalResponse;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.TenantSubscriptionResource;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.TenantSubscriptionResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller governing automotive workshop subscription lifecycles, Stripe Checkout, and Billing Portal access.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/billing/subscriptions")
@Tag(name = "Tenant Subscriptions", description = "Endpoints for managing workshop subscription contracts and Stripe billing")
public class TenantSubscriptionsController {

    private final TenantSubscriptionCommandService subscriptionCommandService;
    private final TenantSubscriptionQueryService subscriptionQueryService;
    private final SubscriptionPlanQueryService planQueryService;
    private final TenantSubscriptionResourceAssembler subscriptionResourceAssembler;

    public TenantSubscriptionsController(
            TenantSubscriptionCommandService subscriptionCommandService,
            TenantSubscriptionQueryService subscriptionQueryService,
            SubscriptionPlanQueryService planQueryService,
            TenantSubscriptionResourceAssembler subscriptionResourceAssembler
    ) {
        this.subscriptionCommandService = Objects.requireNonNull(subscriptionCommandService, "TenantSubscriptionCommandService cannot be null");
        this.subscriptionQueryService = Objects.requireNonNull(subscriptionQueryService, "TenantSubscriptionQueryService cannot be null");
        this.planQueryService = Objects.requireNonNull(planQueryService, "SubscriptionPlanQueryService cannot be null");
        this.subscriptionResourceAssembler = Objects.requireNonNull(subscriptionResourceAssembler, "TenantSubscriptionResourceAssembler cannot be null");
    }

    /**
     * Retrieves current active subscription, status, and quota ceilings for the authenticated workshop tenant.
     */
    @Operation(summary = "Get current workshop subscription")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('billing:subscriptions:read') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<TenantSubscriptionResource> getCurrentTenantSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        TenantSubscription subscription = subscriptionQueryService.handle(new GetTenantSubscriptionQuery(tenantId))
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription found for workshop: " + tenantId.value()));

        Optional<SubscriptionPlan> planOptional = planQueryService.handle(new GetSubscriptionPlanByIdQuery(subscription.planId()));

        return ResponseEntity.ok(subscriptionResourceAssembler.toResource(subscription, planOptional.orElse(null)));
    }

    /**
     * Initializes a hosted Stripe Checkout Session for subscription purchase or plan upgrade.
     */
    @Operation(summary = "Create Stripe Checkout session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout session created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate subscription conflict")
    })
    @PostMapping("/checkout-session")
    @PreAuthorize("hasAuthority('billing:subscriptions:manage_stripe') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCheckoutSessionRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails);

        InitiateCheckoutSessionCommand command = new InitiateCheckoutSessionCommand(
                tenantId,
                new PlanId(request.planId()),
                request.successUrl(),
                request.cancelUrl()
        );

        String checkoutUrl = subscriptionCommandService.handle(command);
        String sessionId = extractSessionIdFromUrl(checkoutUrl);

        return ResponseEntity.ok(new CheckoutSessionResponse(checkoutUrl, sessionId));
    }

    /**
     * Generates a hosted Stripe Customer Portal URL for self-service payment management.
     */
    @Operation(summary = "Create Stripe Customer Portal session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer portal session created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Customer or subscription not found")
    })
    @PostMapping("/customer-portal")
    @PreAuthorize("hasAuthority('billing:subscriptions:manage_stripe') or hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<CustomerPortalResponse> createCustomerPortalSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CustomerPortalRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        String portalUrl = subscriptionCommandService.handleCreateCustomerPortalSession(tenantId, request.returnUrl());
        return ResponseEntity.ok(new CustomerPortalResponse(portalUrl));
    }


    /**
     * Cancels an active subscription immediately or schedules termination at current period end.
     */
    @Operation(summary = "Cancel subscription or schedule period-end termination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription cancellation scheduled at period end"),
            @ApiResponse(responseCode = "204", description = "Subscription canceled immediately"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Requires ROLE_WORKSHOP_OWNER)"),
            @ApiResponse(responseCode = "404", description = "Subscription not found"),
            @ApiResponse(responseCode = "409", description = "Subscription already canceled")
    })
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('billing:subscriptions:manage_stripe') and hasRole('ROLE_WORKSHOP_OWNER')")
    public ResponseEntity<TenantSubscriptionResource> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CancelSubscriptionRequest request
    ) {
        TenantId tenantId = resolveTenantId(userDetails);
        TenantSubscription current = subscriptionQueryService.handle(new GetTenantSubscriptionQuery(tenantId))
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription found for workshop: " + tenantId.value()));

        CancelSubscriptionCommand command = new CancelSubscriptionCommand(current.id(), request.cancelImmediately(), request.cancellationReason());
        subscriptionCommandService.handle(command);

        if (request.cancelImmediately()) {
            return ResponseEntity.noContent().build();
        }

        TenantSubscription updated = subscriptionQueryService.handle(new GetTenantSubscriptionQuery(tenantId))
                .orElseThrow(() -> new SubscriptionNotFoundException("No subscription found for workshop: " + tenantId.value()));
        Optional<SubscriptionPlan> plan = planQueryService.handle(new GetSubscriptionPlanByIdQuery(updated.planId()));

        return ResponseEntity.ok(subscriptionResourceAssembler.toResource(updated, plan.orElse(null)));
    }

    private TenantId resolveTenantId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getTenantId() == null) {
            throw new IllegalArgumentException("Tenant context could not be resolved from authenticated principal");
        }
        return new TenantId(userDetails.getTenantId());
    }

    private String extractSessionIdFromUrl(String url) {
        if (url != null && !url.isBlank()) {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < url.length() - 1) {
                String candidate = url.substring(lastSlash + 1);
                if (candidate.startsWith("cs_")) {
                    return candidate;
                }
            }
        }
        return "cs_" + UUID.randomUUID().toString().replace("-", "");
    }
}
