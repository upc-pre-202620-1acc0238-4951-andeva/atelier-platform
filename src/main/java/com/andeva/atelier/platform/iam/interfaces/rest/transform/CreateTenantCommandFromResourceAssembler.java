package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.CreateTenantResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Assembler transforming {@link CreateTenantResource} into {@link CreateTenantCommand},
 * securing the administrator's plaintext password with BCrypt hashing.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CreateTenantCommandFromResourceAssembler {

    private final BCryptHashingService hashingService;

    public CreateTenantCommandFromResourceAssembler(BCryptHashingService hashingService) {
        this.hashingService = Objects.requireNonNull(hashingService, "BCryptHashingService cannot be null");
    }

    /**
     * Converts a {@link CreateTenantResource} into a domain {@link CreateTenantCommand}.
     *
     * @param resource Incoming REST request DTO
     * @return Initialized domain command
     */
    public CreateTenantCommand toCommandFromResource(CreateTenantResource resource) {
        Objects.requireNonNull(resource, "CreateTenantResource cannot be null");
        String hashedPassword = hashingService.hash(resource.adminPassword());
        return toCommand(resource, hashedPassword);
    }

    /**
     * Overloaded static helper for creating command with pre-hashed password.
     *
     * @param resource       Incoming REST request DTO
     * @param hashedPassword Pre-hashed BCrypt password
     * @return Initialized domain command
     */
    public static CreateTenantCommand toCommand(CreateTenantResource resource, String hashedPassword) {
        Objects.requireNonNull(resource, "CreateTenantResource cannot be null");
        Objects.requireNonNull(hashedPassword, "hashedPassword cannot be null");

        PhoneNumber phone = (resource.adminPhone() != null && !resource.adminPhone().isBlank())
                ? PhoneNumber.of(resource.adminPhone())
                : null;

        return new CreateTenantCommand(
                resource.name().trim(),
                resource.legalName().trim(),
                TaxId.of(resource.taxId()),
                EmailAddress.of(resource.adminEmail()),
                Password.of(hashedPassword),
                PersonName.of(resource.adminFirstName(), resource.adminLastName()),
                phone
        );
    }
}
