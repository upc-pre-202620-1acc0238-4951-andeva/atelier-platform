package com.andeva.atelier.platform.operations.domain.model.aggregates;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public class Service extends AbstractDomainAggregateRoot<Service> {
    private final ServiceId id;
    private final TenantId tenantId;
    private String name;
    private Money basePrice;
    private int estimatedDurationMinutes;

    public Service(ServiceId id, TenantId tenantId, String name, Money basePrice, int estimatedDurationMinutes) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        setName(name);
        setBasePrice(basePrice);
        setEstimatedDurationMinutes(estimatedDurationMinutes);
    }

    public static Service create(TenantId tenantId, String name, Money basePrice, int estimatedDurationMinutes) {
        return new Service(ServiceId.generate(), tenantId, name, basePrice, estimatedDurationMinutes);
    }

    public void updateDetails(String newName, Money newBasePrice, int newEstimatedMinutes) {
        setName(newName);
        setBasePrice(newBasePrice);
        setEstimatedDurationMinutes(newEstimatedMinutes);
    }

    private void setName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        String trimmed = name.trim();
        if (trimmed.length() < 3 || trimmed.length() > 150) {
            throw new IllegalArgumentException("Service name must be between 3 and 150 characters");
        }
        this.name = trimmed;
    }

    private void setBasePrice(Money basePrice) {
        Objects.requireNonNull(basePrice, "basePrice cannot be null");
        if (basePrice.amount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Service base price cannot be negative");
        }
        this.basePrice = basePrice;
    }

    private void setEstimatedDurationMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("estimatedDurationMinutes must be positive: " + minutes);
        }
        this.estimatedDurationMinutes = minutes;
    }

    public ServiceId getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getName() { return name; }
    public Money getBasePrice() { return basePrice; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
}
