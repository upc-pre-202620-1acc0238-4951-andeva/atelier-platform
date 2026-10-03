package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.SubscriptionPlanCommandService;
import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.commands.CreateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.UpdateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListActivePlansQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CreateSubscriptionPlanRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.UpdateSubscriptionPlanRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SubscriptionPlanResource;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.SubscriptionPlanResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * REST controller governing the commercial SaaS subscription plan catalog and quota configuration.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/billing/plans")
@Tag(name = "Subscription Plans", description = "Endpoints for commercial SaaS plan catalog and quota configuration")
public class SubscriptionPlansController {

    private final SubscriptionPlanCommandService planCommandService;
    private final SubscriptionPlanQueryService planQueryService;
    private final SubscriptionPlanResourceAssembler planResourceAssembler;

    public SubscriptionPlansController(
            SubscriptionPlanCommandService planCommandService,
            SubscriptionPlanQueryService planQueryService,
            SubscriptionPlanResourceAssembler planResourceAssembler
    ) {
        this.planCommandService = Objects.requireNonNull(planCommandService, "SubscriptionPlanCommandService cannot be null");
        this.planQueryService = Objects.requireNonNull(planQueryService, "SubscriptionPlanQueryService cannot be null");
        this.planResourceAssembler = Objects.requireNonNull(planResourceAssembler, "SubscriptionPlanResourceAssembler cannot be null");
    }

    /**
     * Lists all commercial subscription plans currently active for onboarding.
     */
    @Operation(summary = "Get all active subscription plans")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active plans successfully retrieved"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResource>> getAllActivePlans(
            @RequestParam(required = false) String billingCycle
    ) {
        List<SubscriptionPlan> plans = planQueryService.handle(new ListActivePlansQuery());

        if (billingCycle != null && !billingCycle.isBlank()) {
            plans = plans.stream()
                    .filter(p -> p.pricing().billingCycle().name().equalsIgnoreCase(billingCycle.trim()))
                    .toList();
        }

        return ResponseEntity.ok(planResourceAssembler.toResourceList(plans));
    }

    /**
     * Retrieves commercial and technical quota specifications for a single subscription plan by ID.
     */
    @Operation(summary = "Get subscription plan by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan details successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResource> getPlanById(@PathVariable UUID id) {
        SubscriptionPlan plan = planQueryService.handle(new GetSubscriptionPlanByIdQuery(new PlanId(id)))
                .orElseThrow(() -> new PlanNotFoundException(new PlanId(id)));

        return ResponseEntity.ok(planResourceAssembler.toResource(plan));
    }

    /**
     * Creates and registers a new commercial subscription plan linked to a Stripe Price ID.
     */
    @Operation(summary = "Create a new commercial subscription plan")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Requires ROLE_SUPER_ADMIN)"),
            @ApiResponse(responseCode = "409", description = "Duplicate plan name or Stripe price ID"),
            @ApiResponse(responseCode = "422", description = "Invalid plan pricing or quota limits")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('billing:plans:write') and hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<SubscriptionPlanResource> createPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {
        PlanTier tier = PlanTier.valueOf(request.tier().toUpperCase());
        Currency currency = Currency.valueOf(request.currency().toUpperCase());
        Money price = Money.of(request.price(), currency);
        BillingCycle cycle = BillingCycle.valueOf(request.billingCycle().toUpperCase());
        TenantQuotaLimits quotas = mapQuotas(request.quotaLimits());

        CreateSubscriptionPlanCommand command = new CreateSubscriptionPlanCommand(
                new StripePriceId(request.stripePriceId()),
                request.name(),
                tier,
                price,
                cycle,
                quotas
        );

        PlanId planId = planCommandService.handle(command);

        SubscriptionPlan createdPlan = planQueryService.handle(new GetSubscriptionPlanByIdQuery(planId))
                .orElseThrow(() -> new PlanNotFoundException(planId));

        return ResponseEntity.created(URI.create("/api/v1/billing/plans/" + planId.value()))
                .body(planResourceAssembler.toResource(createdPlan));
    }

    /**
     * Updates commercial parameters, pricing, and quota limits of an existing plan.
     */
    @Operation(summary = "Update an existing commercial subscription plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (Requires ROLE_SUPER_ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "422", description = "Invalid plan pricing or quota limits")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('billing:plans:write') and hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<SubscriptionPlanResource> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        PlanId planId = new PlanId(id);
        SubscriptionPlan existing = planQueryService.handle(new GetSubscriptionPlanByIdQuery(planId))
                .orElseThrow(() -> new PlanNotFoundException(planId));

        Money price = Money.of(request.price(), existing.pricing().price().currency());
        BillingCycle cycle = BillingCycle.valueOf(request.billingCycle().toUpperCase());
        TenantQuotaLimits quotas = mapQuotas(request.quotaLimits());

        UpdateSubscriptionPlanCommand command = new UpdateSubscriptionPlanCommand(
                planId,
                request.name(),
                price,
                cycle,
                quotas
        );

        planCommandService.handle(command);

        if (request.isActive() != null) {
            if (request.isActive()) {
                planCommandService.handleActivate(planId);
            } else {
                planCommandService.handleDeactivate(planId);
            }
        }

        SubscriptionPlan updatedPlan = planQueryService.handle(new GetSubscriptionPlanByIdQuery(planId))
                .orElseThrow(() -> new PlanNotFoundException(planId));

        return ResponseEntity.ok(planResourceAssembler.toResource(updatedPlan));
    }

    private TenantQuotaLimits mapQuotas(com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto dto) {
        if (dto == null) {
            return TenantQuotaLimits.goPlanPreset();
        }
        return new TenantQuotaLimits(
                dto.maxBranches(),
                dto.maxActiveStaff(),
                dto.maxActiveObd2Devices(),
                dto.maxPhotosPerWorkOrder(),
                dto.maxMonthlyAiReports(),
                dto.companyRegistrationAllowed(),
                dto.multiWarehouseAllowed(),
                dto.marketplaceListed(),
                dto.maxMonthlyWorkOrders(),
                dto.iotTelemetryEnabled(),
                dto.aiDiagnosticsEnabled()
        );
    }
}
