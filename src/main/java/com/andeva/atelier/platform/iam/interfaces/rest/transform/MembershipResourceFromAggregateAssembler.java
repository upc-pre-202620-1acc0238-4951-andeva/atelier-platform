package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.MembershipResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RoleResource;

import java.util.List;
import java.util.Objects;

/**
 * Assembler projecting domain {@link TenantMembership} aggregates into REST {@link MembershipResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class MembershipResourceFromAggregateAssembler {

    private MembershipResourceFromAggregateAssembler() {
    }

    /**
     * Converts a {@link TenantMembership} and associated {@link User} into a {@link MembershipResource}.
     *
     * @param membership Domain tenant membership aggregate
     * @param user       Associated user aggregate carrying profile demographics (or null)
     * @return REST response projection
     */
    public static MembershipResource toResourceFromAggregate(TenantMembership membership, User user) {
        Objects.requireNonNull(membership, "TenantMembership cannot be null");

        String employeeName = (user != null && user.profile() != null)
                ? user.profile().getFullName()
                : "Staff Member";

        String email = (user != null && user.email() != null)
                ? user.email().value()
                : "";

        return toResourceFromAggregate(membership, employeeName, email);
    }

    /**
     * Converts a {@link TenantMembership} with explicit name and email into a {@link MembershipResource}.
     *
     * @param membership   Domain tenant membership aggregate
     * @param employeeName Full employee name
     * @param email        Access email address
     * @return REST response projection
     */
    public static MembershipResource toResourceFromAggregate(TenantMembership membership, String employeeName, String email) {
        Objects.requireNonNull(membership, "TenantMembership cannot be null");

        List<RoleResource> roleResources = membership.assignedRoles().stream()
                .map(RoleResourceFromAggregateAssembler::toResourceFromAggregate)
                .toList();

        return new MembershipResource(
                membership.id().value(),
                membership.tenantId().value(),
                membership.userId().value(),
                employeeName,
                email,
                membership.status().name(),
                membership.salaryType().name(),
                membership.baseSalary().amount(),
                membership.baseSalary().currency().name(),
                roleResources
        );
    }
}
