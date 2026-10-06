package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.inventory.domain.model.enums.PurchaseOrderStatus;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.PurchaseOrderPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderPersistenceRepository extends JpaRepository<PurchaseOrderPersistenceEntity, UUID> {

    Optional<PurchaseOrderPersistenceEntity> findByTenantIdAndOrderNumber(UUID tenantId, String orderNumber);

    List<PurchaseOrderPersistenceEntity> findByTenantId(UUID tenantId);

    List<PurchaseOrderPersistenceEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);

    List<PurchaseOrderPersistenceEntity> findByTenantIdAndStatus(UUID tenantId, PurchaseOrderStatus status);

    @Query("SELECT po FROM PurchaseOrderPersistenceEntity po WHERE po.tenantId = :tenantId " +
            "AND (:branchId IS NULL OR po.branchId = :branchId) " +
            "AND (:status IS NULL OR po.status = :status) " +
            "AND po.deletedAt IS NULL ORDER BY po.createdAt DESC")
    List<PurchaseOrderPersistenceEntity> findByTenantBranchAndStatus(@Param("tenantId") UUID tenantId,
                                                                    @Param("branchId") UUID branchId,
                                                                    @Param("status") PurchaseOrderStatus status);
}
