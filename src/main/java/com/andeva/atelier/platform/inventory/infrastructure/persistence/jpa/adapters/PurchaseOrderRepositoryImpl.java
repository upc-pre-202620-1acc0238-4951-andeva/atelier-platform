package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.PurchaseOrderNumber;
import com.andeva.atelier.platform.inventory.domain.repositories.PurchaseOrderRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.PurchaseOrderPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.PurchaseOrderPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    private final PurchaseOrderPersistenceRepository purchaseOrderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PurchaseOrderRepositoryImpl(
            PurchaseOrderPersistenceRepository purchaseOrderRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository, "purchaseOrderRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<PurchaseOrder> findById(PurchaseOrderId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return purchaseOrderRepository.findById(id.value())
                .map(PurchaseOrderPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<PurchaseOrder> findByTenantIdAndOrderNumber(TenantId tenantId, PurchaseOrderNumber orderNumber) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(orderNumber, "orderNumber cannot be null");
        return purchaseOrderRepository.findByTenantIdAndOrderNumber(tenantId.value(), orderNumber.value())
                .map(PurchaseOrderPersistenceAssembler::toDomain);
    }

    @Override
    public List<PurchaseOrder> findByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return purchaseOrderRepository.findByTenantId(tenantId.value()).stream()
                .map(PurchaseOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<PurchaseOrder> findByTenantIdAndBranchId(TenantId tenantId, BranchId branchId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        return purchaseOrderRepository.findByTenantIdAndBranchId(tenantId.value(), branchId.value()).stream()
                .map(PurchaseOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<PurchaseOrder> findByTenantIdAndStatus(TenantId tenantId, PurchaseOrderStatus status) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        return purchaseOrderRepository.findByTenantIdAndStatus(tenantId.value(), status).stream()
                .map(PurchaseOrderPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public PurchaseOrder save(PurchaseOrder order) {
        Objects.requireNonNull(order, "order cannot be null");
        PurchaseOrderPersistenceEntity entity = purchaseOrderRepository.findById(order.getId().value())
                .map(existing -> {
                    PurchaseOrderPersistenceAssembler.updateEntity(existing, order);
                    return existing;
                })
                .orElseGet(() -> PurchaseOrderPersistenceAssembler.toEntity(order));

        PurchaseOrderPersistenceEntity saved = purchaseOrderRepository.save(entity);
        PurchaseOrder domain = PurchaseOrderPersistenceAssembler.toDomain(saved);

        order.domainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();

        return domain;
    }

    @Override
    public void delete(PurchaseOrder order) {
        Objects.requireNonNull(order, "order cannot be null");
        purchaseOrderRepository.deleteById(order.getId().value());
    }
}
