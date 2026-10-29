package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;

import java.util.UUID;

/**
 * Display names shown next to a task: its catalog service and the spare parts charged to it.
 */
public interface WorkOrderTaskLabelsQueryService {

    String SERVICE_FALLBACK = "Service";

    /**
     * Fallback that only formats ids, for callers wired without the catalogs.
     */
    WorkOrderTaskLabelsQueryService IDS_ONLY = new WorkOrderTaskLabelsQueryService() {
        @Override
        public String serviceNameOf(WorkOrderTask task) {
            return SERVICE_FALLBACK;
        }

        @Override
        public String productNameOf(UUID tenantId, UUID productId) {
            return productFallback(productId);
        }
    };

    String serviceNameOf(WorkOrderTask task);

    /**
     * @param tenantId workshop owning the inventory item; without it only the id is shown
     */
    String productNameOf(UUID tenantId, UUID productId);

    static String productFallback(UUID productId) {
        return "Product " + productId;
    }
}
