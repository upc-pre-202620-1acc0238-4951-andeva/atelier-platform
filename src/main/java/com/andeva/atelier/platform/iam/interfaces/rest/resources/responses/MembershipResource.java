package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response projection representing a staff member's contractual affiliation and assigned roles in a tenant.
 *
 * @param id           Universal unique identifier of the membership
 * @param tenantId     Universal identifier of the employer workshop tenant
 * @param userId       Universal identifier of the user account
 * @param employeeName Full legal name of the employee
 * @param email        Corporate or access email address
 * @param status       Employment status (ACTIVE or INACTIVE)
 * @param salaryType   Remuneration modality (FIXED or HOURLY)
 * @param baseSalary   Nominal base compensation amount
 * @param currency     Remuneration currency (PEN or USD)
 * @param roles        Assigned RBAC roles
 * @author Joel Huamani Estefanero
 */
public record MembershipResource(
        UUID id,
        UUID tenantId,
        UUID userId,
        String employeeName,
        String email,
        String status,
        String salaryType,
        BigDecimal baseSalary,
        String currency,
        List<RoleResource> roles
) {
    public MembershipResource {
        roles = roles != null ? List.copyOf(roles) : List.of();
    }
}
