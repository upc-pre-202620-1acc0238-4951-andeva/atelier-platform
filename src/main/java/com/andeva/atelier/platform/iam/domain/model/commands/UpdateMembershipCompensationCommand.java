package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.util.Objects;

/**
 * Domain command to adjust staff member compensation scheme and base amount.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateMembershipCompensationCommand(
        TenantMembershipId membershipId,
        SalaryType salaryType,
        Money baseSalary
) {
    public UpdateMembershipCompensationCommand {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(salaryType, "Salary type cannot be null");
        Objects.requireNonNull(baseSalary, "Base salary cannot be null");
    }
}
