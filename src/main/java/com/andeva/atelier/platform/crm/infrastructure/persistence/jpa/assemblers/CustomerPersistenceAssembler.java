package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Locale;

public final class CustomerPersistenceAssembler {

    private CustomerPersistenceAssembler() {
    }

    public static Customer toDomain(CustomerPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        PersonName name = null;
        if (entity.getType() == CustomerType.INDIVIDUAL && entity.getFirstName() != null && entity.getLastName() != null) {
            name = PersonName.of(entity.getFirstName(), entity.getLastName());
        }

        CustomerStatus status = entity.getStatus() != null
                ? CustomerStatus.valueOf(entity.getStatus().trim().toUpperCase(Locale.ROOT))
                : CustomerStatus.ACTIVE;

        EmailAddress email = entity.getEmail() != null ? EmailAddress.of(entity.getEmail()) : null;
        PhoneNumber phone = entity.getPhone() != null ? PhoneNumber.of(entity.getPhone()) : null;
        TaxId taxId = TaxId.of(entity.getTaxId());

        return new Customer(
                CustomerId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getType(),
                name,
                entity.getCompanyName(),
                taxId,
                email,
                phone,
                status
        );
    }

    public static CustomerPersistenceEntity toEntity(Customer domain) {
        if (domain == null) {
            return null;
        }

        String firstName = domain.name() != null ? domain.name().firstName() : null;
        String lastName = domain.name() != null ? domain.name().lastName() : null;
        String email = domain.email() != null ? domain.email().value() : null;
        String phone = domain.phone() != null ? domain.phone().value() : null;

        return new CustomerPersistenceEntity(
                domain.id().value(),
                domain.tenantId().value(),
                domain.type(),
                firstName,
                lastName,
                domain.companyName(),
                domain.taxId().value(),
                email,
                phone,
                domain.status().name().toLowerCase(Locale.ROOT)
        );
    }
}
