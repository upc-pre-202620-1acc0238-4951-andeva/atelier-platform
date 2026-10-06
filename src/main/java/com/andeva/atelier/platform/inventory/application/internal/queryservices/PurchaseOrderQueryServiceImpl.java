package com.andeva.atelier.platform.inventory.application.internal.queryservices;

import com.andeva.atelier.platform.inventory.application.queryservices.PurchaseOrderQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrderDetailQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetPurchaseOrdersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.repositories.PurchaseOrderRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.PurchaseOrderPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.PurchaseOrderPersistenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderQueryServiceImpl implements PurchaseOrderQueryService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderPersistenceRepository purchaseOrderPersistenceRepository;

    public PurchaseOrderQueryServiceImpl(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderPersistenceRepository purchaseOrderPersistenceRepository
    ) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "purchaseOrderRepository cannot be null");
        this.purchaseOrderPersistenceRepository = Objects.requireNonNull(purchaseOrderPersistenceRepository, "purchaseOrderPersistenceRepository cannot be null");
    }

    @Override
    public Optional<PurchaseOrder> handle(GetPurchaseOrderByIdQuery query) {
        Objects.requireNonNull(query, "GetPurchaseOrderByIdQuery cannot be null");
        return purchaseOrderRepository.findById(query.purchaseOrderId());
    }

    @Override
    public Optional<PurchaseOrder> handle(GetPurchaseOrderDetailQuery query) {
        Objects.requireNonNull(query, "GetPurchaseOrderDetailQuery cannot be null");
        return purchaseOrderRepository.findById(query.purchaseOrderId());
    }

    @Override
    public List<PurchaseOrder> handle(GetPurchaseOrdersByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetPurchaseOrdersByTenantIdQuery cannot be null");
        return purchaseOrderPersistenceRepository.findByTenantBranchAndStatus(
                query.tenantId().value(),
                query.branchId() != null ? query.branchId().value() : null,
                query.status()
        ).stream().map(PurchaseOrderPersistenceAssembler::toDomain).toList();
    }
}
