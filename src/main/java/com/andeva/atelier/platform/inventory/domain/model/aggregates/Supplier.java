package com.andeva.atelier.platform.inventory.domain.model.aggregates;

import com.andeva.atelier.platform.inventory.domain.model.events.SupplierRegisteredEvent;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.Optional;

public class Supplier extends AbstractDomainAggregateRoot<Supplier> {

    private final SupplierId id;
    private final TenantId tenantId;
    private String businessName;
    private final TaxId taxId;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private boolean active;

    public Supplier(
            SupplierId id,
            TenantId tenantId,
            String businessName,
            TaxId taxId,
            String contactName,
            String phone,
            String email,
            String address,
            boolean active
    ) {
        this.id = Objects.requireNonNull(id, "Supplier id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.businessName = Objects.requireNonNull(businessName, "BusinessName cannot be null");
        this.taxId = Objects.requireNonNull(taxId, "TaxId cannot be null");
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.active = active;
    }

    public static Supplier register(
            TenantId tenantId,
            String businessName,
            TaxId taxId,
            String contactName,
            String phone,
            String email,
            String address
    ) {
        Supplier supplier = new Supplier(
                SupplierId.generate(),
                tenantId,
                businessName,
                taxId,
                contactName,
                phone,
                email,
                address,
                true
        );
        supplier.registerDomainEvent(SupplierRegisteredEvent.of(supplier.id, tenantId, businessName, taxId));
        return supplier;
    }

    public static Supplier reconstitute(
            SupplierId id,
            TenantId tenantId,
            String businessName,
            TaxId taxId,
            String contactName,
            String phone,
            String email,
            String address,
            boolean active
    ) {
        return new Supplier(id, tenantId, businessName, taxId, contactName, phone, email, address, active);
    }

    public void updateContactInfo(String businessName, String contactName, String phone, String email, String address) {
        if (businessName != null && !businessName.isBlank()) {
            this.businessName = businessName;
        }
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public SupplierId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getBusinessName() { return businessName; }
    public TaxId getTaxId() { return taxId; }
    public Optional<String> getContactName() { return Optional.ofNullable(contactName); }
    public Optional<String> getPhone() { return Optional.ofNullable(phone); }
    public Optional<String> getEmail() { return Optional.ofNullable(email); }
    public Optional<String> getAddress() { return Optional.ofNullable(address); }
    public boolean isActive() { return active; }
}
