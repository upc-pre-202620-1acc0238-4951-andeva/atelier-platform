package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.enums.TenantStatus;
import com.andeva.atelier.platform.iam.domain.model.events.BranchCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantActivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantRegisteredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantSuspendedEvent;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sovereign multi-tenant aggregate root representing an automotive workshop company.
 * Forms the root of all multi-tenant isolation, branch topology, and billing boundaries.
 *
 * @author Joel Huamani Estefanero
 */
public class Tenant extends AbstractDomainAggregateRoot<Tenant> {

    private final TenantId id;
    private String name;
    private String legalName;
    private final TaxId taxId;
    private TenantStatus status;
    private String stripeCustomerId;
    private final List<Branch> branches;

    public Tenant(
            TenantId id,
            String name,
            String legalName,
            TaxId taxId,
            TenantStatus status,
            String stripeCustomerId,
            List<Branch> branches) {
        this.id = Objects.requireNonNull(id, "Tenant identifier cannot be null");
        this.name = validateName(name);
        this.legalName = validateLegalName(legalName);
        this.taxId = Objects.requireNonNull(taxId, "Tax ID cannot be null");
        this.status = Objects.requireNonNull(status, "Tenant status cannot be null");
        this.stripeCustomerId = stripeCustomerId;
        this.branches = branches != null ? new ArrayList<>(branches) : new ArrayList<>();
    }

    /**
     * Domain factory instantiating a new workshop Tenant in ACTIVE state and registering a TenantRegisteredEvent.
     *
     * @param name      commercial workshop name
     * @param legalName formal corporate legal name (SUNAT registered)
     * @param taxId     validated 11-digit RUC
     * @return initialized Tenant aggregate root
     */
    public static Tenant create(String name, String legalName, TaxId taxId) {
        TenantId tenantId = TenantId.generate();
        Tenant tenant = new Tenant(
                tenantId,
                name,
                legalName,
                taxId,
                TenantStatus.ACTIVE,
                null,
                new ArrayList<>()
        );
        tenant.registerEvent(TenantRegisteredEvent.of(tenantId, name, taxId));
        return tenant;
    }

    /**
     * Associates a customer identifier assigned by the Stripe billing provider.
     */
    public void assignStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = Objects.requireNonNull(stripeCustomerId, "Stripe customer ID cannot be null");
    }

    /**
     * Transitions tenant status to ACTIVE and registers a TenantActivatedEvent.
     */
    public void activate() {
        if (this.status == TenantStatus.ACTIVE) {
            return;
        }
        this.status = TenantStatus.ACTIVE;
        registerEvent(TenantActivatedEvent.of(this.id));
    }

    /**
     * Suspends the workshop tenant for non-payment, legal violation, or administrative action.
     *
     * @param reason explanation for the suspension
     */
    public void suspend(String reason) {
        if (this.status == TenantStatus.SUSPENDED) {
            throw new IllegalStateException("Workshop tenant is already suspended");
        }
        Objects.requireNonNull(reason, "Suspension reason cannot be null");
        this.status = TenantStatus.SUSPENDED;
        registerEvent(TenantSuspendedEvent.of(this.id, reason));
    }

    /**
     * Creates and adds a new physical workshop branch to this tenant.
     *
     * @param name                 branch operational denomination
     * @param sunatCode            4-digit establishment code
     * @param location             WGS84 GPS coordinate
     * @param geofenceRadiusMeters geofence radius in meters
     * @return created Branch entity
     */
    public Branch addBranch(String name, String sunatCode, GeoPoint location, int geofenceRadiusMeters) {
        Branch branch = Branch.create(this.id, name, sunatCode, location, geofenceRadiusMeters);
        this.branches.add(branch);
        registerEvent(BranchCreatedEvent.of(branch.id(), this.id, name));
        return branch;
    }

    /**
     * Updates workshop commercial and legal metadata.
     */
    public void updateProfile(String name, String legalName) {
        this.name = validateName(name);
        this.legalName = validateLegalName(legalName);
    }

    /**
     * Looks up an internal branch entity by its unique branch identifier.
     */
    public Optional<Branch> findBranchById(BranchId branchId) {
        Objects.requireNonNull(branchId, "Branch identifier cannot be null");
        return this.branches.stream()
                .filter(b -> b.id().equals(branchId))
                .findFirst();
    }

    public TenantId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String legalName() {
        return legalName;
    }

    public TaxId taxId() {
        return taxId;
    }

    public TenantStatus status() {
        return status;
    }

    public String stripeCustomerId() {
        return stripeCustomerId;
    }

    public List<Branch> branches() {
        return Collections.unmodifiableList(branches);
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "Workshop name cannot be null");
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Workshop name cannot be empty");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Workshop name cannot exceed 100 characters");
        }
        return trimmed;
    }

    private static String validateLegalName(String legalName) {
        Objects.requireNonNull(legalName, "Workshop legal name cannot be null");
        String trimmed = legalName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Workshop legal name cannot be empty");
        }
        if (trimmed.length() > 150) {
            throw new IllegalArgumentException("Workshop legal name cannot exceed 150 characters");
        }
        return trimmed;
    }
}
