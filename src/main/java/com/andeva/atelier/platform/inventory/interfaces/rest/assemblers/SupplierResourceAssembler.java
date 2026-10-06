package com.andeva.atelier.platform.inventory.interfaces.rest.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.RegisterSupplierResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.UpdateSupplierResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.SupplierResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;

public final class SupplierResourceAssembler {

    private SupplierResourceAssembler() {
    }

    public static RegisterSupplierCommand toCommand(TenantId tenantId, RegisterSupplierResource resource) {
        return new RegisterSupplierCommand(
                tenantId,
                resource.businessName(),
                TaxId.of(resource.taxId()),
                resource.contactName(),
                resource.phone(),
                resource.email(),
                resource.address()
        );
    }

    public static UpdateSupplierCommand toCommand(SupplierId supplierId, UpdateSupplierResource resource) {
        return new UpdateSupplierCommand(
                supplierId,
                resource.businessName(),
                resource.contactName(),
                resource.phone(),
                resource.email(),
                resource.address()
        );
    }

    public static SupplierResource toResource(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        return new SupplierResource(
                supplier.getId().value(),
                supplier.getTenantId().value(),
                supplier.getBusinessName(),
                supplier.getTaxId().value(),
                supplier.getContactName().orElse(null),
                supplier.getPhone().orElse(null),
                supplier.getEmail().orElse(null),
                supplier.getAddress().orElse(null),
                supplier.isActive()
        );
    }

    public static List<SupplierResource> toResourceList(List<Supplier> suppliers) {
        if (suppliers == null) {
            return List.of();
        }
        return suppliers.stream().map(SupplierResourceAssembler::toResource).toList();
    }
}
