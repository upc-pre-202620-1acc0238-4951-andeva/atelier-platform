package com.andeva.atelier.platform.crm.domain.model.aggregates;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerContactUpdatedEvent;
import com.andeva.atelier.platform.crm.domain.model.events.CustomerRegisteredEvent;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Sovereign Aggregate Root representing a commercial client profile (Individual or Company).
 *
 * @author Adiel Sanchez Santin
 */
public class Customer extends AbstractDomainAggregateRoot<Customer> {

    private final CustomerId id;
    private final TenantId tenantId;
    private final CustomerType type;
    private PersonName name;
    private String companyName;
    private final TaxId taxId;
    private EmailAddress email;
    private PhoneNumber phone;
    private CustomerStatus status;

    public Customer(
            CustomerId id,
            TenantId tenantId,
            CustomerType type,
            PersonName name,
            String companyName,
            TaxId taxId,
            EmailAddress email,
            PhoneNumber phone,
            CustomerStatus status
    ) {
        this.id = Objects.requireNonNull(id, "CustomerId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.type = Objects.requireNonNull(type, "CustomerType cannot be null");
        this.taxId = Objects.requireNonNull(taxId, "TaxId cannot be null");
        this.status = Objects.requireNonNull(status, "CustomerStatus cannot be null");

        if (type == CustomerType.INDIVIDUAL) {
            Objects.requireNonNull(name, "PersonName cannot be null for individual customer");
            if (companyName != null && !companyName.isBlank()) {
                throw new IllegalArgumentException("CompanyName must be null for individual customer");
            }
        } else {
            Objects.requireNonNull(companyName, "CompanyName cannot be null for company customer");
            if (companyName.trim().isEmpty()) {
                throw new IllegalArgumentException("CompanyName cannot be empty for company customer");
            }
            if (name != null) {
                throw new IllegalArgumentException("PersonName must be null for company customer");
            }
        }

        if (email == null && phone == null) {
            throw new IllegalArgumentException("At least one contact channel (email or phone) must be provided");
        }

        this.name = name;
        this.companyName = companyName != null ? companyName.trim() : null;
        this.email = email;
        this.phone = phone;
    }

    public static Customer registerIndividual(
            CustomerId id,
            TenantId tenantId,
            PersonName name,
            TaxId taxId,
            EmailAddress email,
            PhoneNumber phone
    ) {
        Customer customer = new Customer(
                id,
                tenantId,
                CustomerType.INDIVIDUAL,
                name,
                null,
                taxId,
                email,
                phone,
                CustomerStatus.ACTIVE
        );
        customer.registerEvent(CustomerRegisteredEvent.of(id, tenantId, CustomerType.INDIVIDUAL, name.getFullName(), taxId));
        return customer;
    }

    public static Customer registerCompany(
            CustomerId id,
            TenantId tenantId,
            String companyName,
            TaxId taxId,
            EmailAddress email,
            PhoneNumber phone
    ) {
        Customer customer = new Customer(
                id,
                tenantId,
                CustomerType.COMPANY,
                null,
                companyName,
                taxId,
                email,
                phone,
                CustomerStatus.ACTIVE
        );
        customer.registerEvent(CustomerRegisteredEvent.of(id, tenantId, CustomerType.COMPANY, companyName.trim(), taxId));
        return customer;
    }

    public void updateContact(EmailAddress newEmail, PhoneNumber newPhone) {
        if (newEmail == null && newPhone == null) {
            throw new IllegalArgumentException("At least one contact method must be provided");
        }
        this.email = newEmail;
        this.phone = newPhone;
        registerEvent(CustomerContactUpdatedEvent.of(this.id, newEmail, newPhone));
    }

    public void updateProfile(PersonName newName) {
        if (this.type != CustomerType.INDIVIDUAL) {
            throw new IllegalStateException("Cannot update individual profile on a corporate company customer");
        }
        this.name = Objects.requireNonNull(newName, "New person name cannot be null");
    }

    public void updateCompanyDetails(String newCompanyName) {
        if (this.type != CustomerType.COMPANY) {
            throw new IllegalStateException("Cannot update corporate company name on an individual customer");
        }
        Objects.requireNonNull(newCompanyName, "New company name cannot be null");
        if (newCompanyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        this.companyName = newCompanyName.trim();
    }

    public void activate() {
        this.status = CustomerStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
    }

    public void archive() {
        deactivate();
    }

    public String getDisplayName() {
        if (this.type == CustomerType.COMPANY) {
            return this.companyName;
        }
        return this.name != null ? this.name.getFullName() : "";
    }

    public CustomerId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public CustomerType type() {
        return type;
    }

    public PersonName name() {
        return name;
    }

    public String companyName() {
        return companyName;
    }

    public TaxId taxId() {
        return taxId;
    }

    public EmailAddress email() {
        return email;
    }

    public PhoneNumber phone() {
        return phone;
    }

    public CustomerStatus status() {
        return status;
    }
}
