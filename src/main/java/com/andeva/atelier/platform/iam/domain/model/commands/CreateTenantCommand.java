package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;

import java.util.Objects;

/**
 * Domain command to register and provision an automotive workshop Tenant with its initial administrator.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateTenantCommand(
        String name,
        String legalName,
        TaxId taxId,
        EmailAddress adminEmail,
        Password adminPassword,
        PersonName adminName,
        PhoneNumber adminPhone
) {
    public CreateTenantCommand {
        Objects.requireNonNull(name, "Workshop commercial name cannot be null");
        Objects.requireNonNull(legalName, "Legal name cannot be null");
        Objects.requireNonNull(taxId, "Tax ID cannot be null");
        Objects.requireNonNull(adminEmail, "Administrator email cannot be null");
        Objects.requireNonNull(adminPassword, "Administrator password cannot be null");
        Objects.requireNonNull(adminName, "Administrator name cannot be null");
    }
}
