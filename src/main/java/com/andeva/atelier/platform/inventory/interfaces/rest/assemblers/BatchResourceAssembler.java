package com.andeva.atelier.platform.inventory.interfaces.rest.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.entities.InventoryBatch;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.BatchDeduction;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.BatchDeductionResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.BatchResource;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.StockAllocationResource;

import java.util.List;

public final class BatchResourceAssembler {

    private BatchResourceAssembler() {
    }

    public static BatchResource toResource(InventoryBatch domain) {
        if (domain == null) {
            return null;
        }

        return new BatchResource(
                domain.getId().value(),
                domain.getItemId().value(),
                domain.getSupplierId().map(SupplierId::value).orElse(null),
                domain.getPurchaseOrderId().map(PurchaseOrderId::value).orElse(null),
                domain.getBatchNumber(),
                domain.getInitialQuantity().value(),
                domain.getRemainingQuantity().value(),
                domain.getUnitCost().amount(),
                domain.getArrivalDate(),
                domain.getReceiptImageUrl().map(StorageUrl::value).orElse(null)
        );
    }

    public static List<BatchResource> toResourceList(List<InventoryBatch> batches) {
        if (batches == null) {
            return List.of();
        }
        return batches.stream().map(BatchResourceAssembler::toResource).toList();
    }

    public static StockAllocationResource toAllocationResource(StockAllocation allocation) {
        if (allocation == null) {
            return null;
        }

        List<BatchDeductionResource> deductions = allocation.deductions().stream()
                .map(BatchResourceAssembler::toDeductionResource)
                .toList();

        return new StockAllocationResource(
                allocation.allocationId(),
                allocation.allocatedQuantity().value(),
                allocation.totalCostOfGoodsSold().amount(),
                deductions
        );
    }

    public static BatchDeductionResource toDeductionResource(BatchDeduction deduction) {
        if (deduction == null) {
            return null;
        }

        return new BatchDeductionResource(
                deduction.batchId(),
                deduction.quantityDeducted().value(),
                deduction.unitCost().amount(),
                deduction.subtotal().amount()
        );
    }
}
