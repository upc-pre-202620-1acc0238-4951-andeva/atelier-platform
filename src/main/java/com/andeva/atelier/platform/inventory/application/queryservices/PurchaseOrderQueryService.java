package com.andeva.atelier.platform.inventory.application.queryservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrdersByTenantIdQuery;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderQueryService {

    Optional<PurchaseOrder> handle(GetPurchaseOrderByIdQuery query);

    Optional<PurchaseOrder> handle(GetPurchaseOrderDetailQuery query);

    List<PurchaseOrder> handle(GetPurchaseOrdersByTenantIdQuery query);
}
