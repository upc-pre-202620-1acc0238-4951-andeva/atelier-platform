package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.events.RoleAssignedToMembershipEvent;
import com.andeva.atelier.platform.iam.domain.model.events.RoleRevokedFromMembershipEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipActivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipCreatedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.TenantMembershipDeactivatedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Sovereign employment and authorization contract binding a User to an automotive workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class TenantMembership extends AbstractDomainAggregateRoot<TenantMembership> {

    private final TenantMembershipId id;
    private final TenantId tenantId;
    private final UserId userId;
    private MembershipStatus status;
    private SalaryType salaryType;
    private Money baseSalary;
    private final Set<Role> assignedRoles;

    public TenantMembership(
            TenantMembershipId id,
            TenantId tenantId,
            UserId userId,
            MembershipStatus status,
            SalaryType salaryType,
            Money baseSalary,
            Set<Role> assignedRoles) {
        this.id = Objects.requireNonNull(id, "Membership identifier cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        this.userId = Objects.requireNonNull(userId, "User identifier cannot be null");
        this.status = Objects.requireNonNull(status, "Membership status cannot be null");
        this.salaryType = Objects.requireNonNull(salaryType, "Salary type cannot be null");
        this.baseSalary = validateBaseSalary(baseSalary);
        this.assignedRoles = assignedRoles != null ? new HashSet<>(assignedRoles) : new HashSet<>();

        if (this.status == MembershipStatus.ACTIVE && this.assignedRoles.isEmpty()) {
            throw new IllegalStateException("An active staff membership must be assigned at least one security role");
        }
    }

    /**
     * Domain factory establishing an active employment membership in a workshop.
     */
    public static TenantMembership create(
            TenantId tenantId,
            UserId userId,
            SalaryType salaryType,
            Money baseSalary,
            Set<Role> initialRoles) {
        TenantMembershipId membershipId = TenantMembershipId.generate();
        TenantMembership membership = new TenantMembership(
                membershipId,
                tenantId,
                userId,
                MembershipStatus.ACTIVE,
                salaryType,
                baseSalary,
                initialRoles
        );
        membership.registerEvent(TenantMembershipCreatedEvent.of(membershipId, tenantId, userId));
        return membership;
    }

    /**
     * Assigns an additional security role to this staff member.
     */
    public void assignRole(Role role) {
        Objects.requireNonNull(role, "Role to assign cannot be null");
        this.assignedRoles.add(role);
        registerEvent(RoleAssignedToMembershipEvent.of(this.id, role.id()));
    }

    /**
     * Revokes a security role from this staff member, enforcing that at least one role remains.
     */
    public void revokeRole(RoleId roleId) {
        Objects.requireNonNull(roleId, "Role identifier to revoke cannot be null");
        if (this.assignedRoles.size() <= 1 && this.assignedRoles.stream().anyMatch(r -> r.id().equals(roleId))) {
            throw new IllegalStateException("Cannot revoke role: active membership must retain at least one security role");
        }
        boolean removed = this.assignedRoles.removeIf(r -> r.id().equals(roleId));
        if (removed) {
            registerEvent(RoleRevokedFromMembershipEvent.of(this.id, roleId));
        }
    }

    /**
     * Adjusts the staff member's compensation package.
     */
    public void updateCompensation(SalaryType newSalaryType, Money newBaseSalary) {
        this.salaryType = Objects.requireNonNull(newSalaryType, "Salary type cannot be null");
        this.baseSalary = validateBaseSalary(newBaseSalary);
    }

    /**
     * Activates the employment contract.
     */
    public void activate() {
        if (this.assignedRoles.isEmpty()) {
            throw new IllegalStateException("Cannot activate membership without assigned security roles");
        }
        this.status = MembershipStatus.ACTIVE;
        registerEvent(TenantMembershipActivatedEvent.of(this.id));
    }

    /**
     * Deactivates the employment contract, revoking operational access to the workshop.
     */
    public void deactivate() {
        this.status = MembershipStatus.INACTIVE;
        registerEvent(TenantMembershipDeactivatedEvent.of(this.id));
    }

    /**
     * Evaluates recursively across all assigned roles whether the staff member possesses the specified permission.
     */
    public boolean hasPermission(String permissionName) {
        Objects.requireNonNull(permissionName, "Permission name cannot be null");
        if (this.status != MembershipStatus.ACTIVE) {
            return false;
        }
        return this.assignedRoles.stream()
                .flatMap(r -> r.permissions().stream())
                .anyMatch(p -> p.name().equals(permissionName));
    }

    public TenantMembershipId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public UserId userId() {
        return userId;
    }

    public MembershipStatus status() {
        return status;
    }

    public SalaryType salaryType() {
        return salaryType;
    }

    public Money baseSalary() {
        return baseSalary;
    }

    public Set<Role> assignedRoles() {
        return Collections.unmodifiableSet(assignedRoles);
    }

    private static Money validateBaseSalary(Money baseSalary) {
        Objects.requireNonNull(baseSalary, "Base salary cannot be null");
        if (baseSalary.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base salary cannot be negative");
        }
        return baseSalary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantMembership that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
