package com.andeva.atelier.platform.crm.domain.model.entities;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

/**
 * Domain entity representing corporate membership and delegation of fleet administration rights.
 *
 * @author Adiel Sanchez Santin
 */
public class CustomerMembership {

    private final CustomerMembershipId id;
    private final CustomerId customerId;
    private final UserId userId;
    private FleetRole role;
    private CustomerMembershipStatus status;

    public CustomerMembership(
            CustomerMembershipId id,
            CustomerId customerId,
            UserId userId,
            FleetRole role,
            CustomerMembershipStatus status
    ) {
        this.id = Objects.requireNonNull(id, "CustomerMembershipId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.role = Objects.requireNonNull(role, "FleetRole cannot be null");
        this.status = Objects.requireNonNull(status, "CustomerMembershipStatus cannot be null");
    }

    public static CustomerMembership create(
            CustomerMembershipId id,
            CustomerId customerId,
            UserId userId,
            FleetRole role
    ) {
        return new CustomerMembership(id, customerId, userId, role, CustomerMembershipStatus.ACTIVE);
    }

    public void activate() {
        this.status = CustomerMembershipStatus.ACTIVE;
    }

    public void suspend() {
        this.status = CustomerMembershipStatus.SUSPENDED;
    }

    public void revoke() {
        this.status = CustomerMembershipStatus.REVOKED;
    }

    public void changeRole(FleetRole newRole) {
        this.role = Objects.requireNonNull(newRole, "New fleet role cannot be null");
    }

    public boolean isActive() {
        return this.status == CustomerMembershipStatus.ACTIVE;
    }

    public boolean hasAdminPrivileges() {
        return this.role == FleetRole.FLEET_ADMIN;
    }

    public CustomerMembershipId getId() {
        return id;
    }

    public CustomerMembershipId id() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public UserId getUserId() {
        return userId;
    }

    public UserId userId() {
        return userId;
    }

    public FleetRole getRole() {
        return role;
    }

    public FleetRole role() {
        return role;
    }

    public CustomerMembershipStatus getStatus() {
        return status;
    }

    public CustomerMembershipStatus status() {
        return status;
    }
}
