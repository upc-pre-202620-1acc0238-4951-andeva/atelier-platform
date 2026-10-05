package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.SupplierPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public final class SupplierPersistenceAssembler {

    private SupplierPersistenceAssembler() {
    }

    public static Supplier toDomain(SupplierPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        return Supplier.reconstitute(
                SupplierId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getBusinessName(),
                TaxId.of(entity.getTaxId()),
                entity.getContactName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.isActive()
        );
    }

    public static SupplierPersistenceEntity toEntity(Supplier domain) {
        if (domain == null) {
            return null;
        }

        return new SupplierPersistenceEntity(
                domain.getId().value(),
                domain.getTenantId().value(),
                domain.getBusinessName(),
                domain.getTaxId().value(),
                domain.getContactName().orElse(null),
                domain.getPhone().orElse(null),
                domain.getEmail().orElse(null),
                domain.getAddress().orElse(null),
                domain.isActive()
        );
    }

    public static void updateEntity(SupplierPersistenceEntity entity, Supplier domain) {
        if (entity == null || domain == null) {
            return;
        }

        entity.setBusinessName(domain.getBusinessName());
        entity.setContactName(domain.getContactName().orElse(null));
        entity.setPhone(domain.getPhone().orElse(null));
        entity.setEmail(domain.getEmail().orElse(null));
        entity.setAddress(domain.getAddress().orElse(null));
        entity.setActive(domain.isActive());
    }
}
