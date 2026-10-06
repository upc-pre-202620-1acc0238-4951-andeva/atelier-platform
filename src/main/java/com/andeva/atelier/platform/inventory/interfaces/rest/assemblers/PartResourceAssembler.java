package com.andeva.atelier.platform.inventory.interfaces.rest.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.CreatePartResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests.UpdatePartResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.BatchResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PartDetailResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.PartResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;

public final class PartResourceAssembler {

    private PartResourceAssembler() {
    }

    public static CreateInventoryItemCommand toCommand(TenantId tenantId, CreatePartResource resource) {
        return new CreateInventoryItemCommand(
                tenantId,
                resource.name(),
                Sku.of(resource.sku()),
                resource.category(),
                Money.of(resource.basePrice(), Currency.PEN),
                Quantity.of(resource.minimumStock()),
                resource.unitOfMeasure() != null ? resource.unitOfMeasure() : "UNIT"
        );
    }

    public static UpdateInventoryItemCommand toCommand(InventoryItemId itemId, UpdatePartResource resource) {
        return new UpdateInventoryItemCommand(
                itemId,
                resource.name(),
                resource.category(),
                Money.of(resource.basePrice(), Currency.PEN),
                Quantity.of(resource.minimumStock()),
                resource.unitOfMeasure() != null ? resource.unitOfMeasure() : "UNIT",
                resource.status()
        );
    }

    public static PartResource toResource(InventoryItem item) {
        if (item == null) {
            return null;
        }

        return new PartResource(
                item.getId().value(),
                item.getTenantId().value(),
                item.getName(),
                item.getSku().value(),
                item.getCategory(),
                item.getBasePrice().amount(),
                item.getTotalStock().value(),
                item.getMinimumStock().value(),
                item.getUnitOfMeasure(),
                item.getStatus()
        );
    }

    public static PartDetailResource toDetailResource(InventoryItem item) {
        if (item == null) {
            return null;
        }

        List<BatchResource> batchResources = BatchResourceAssembler.toResourceList(item.getBatches());

        return new PartDetailResource(
                item.getId().value(),
                item.getTenantId().value(),
                item.getName(),
                item.getSku().value(),
                item.getCategory(),
                item.getBasePrice().amount(),
                item.getTotalStock().value(),
                item.getMinimumStock().value(),
                item.getUnitOfMeasure(),
                item.getStatus(),
                batchResources
        );
    }
}
