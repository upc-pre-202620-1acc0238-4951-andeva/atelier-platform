package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryBatchPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryBatchPersistenceRepository extends JpaRepository<InventoryBatchPersistenceEntity, UUID> {

    @Query("SELECT b FROM InventoryBatchPersistenceEntity b WHERE b.item.id = :itemId ORDER BY b.arrivalDate DESC")
    List<InventoryBatchPersistenceEntity> findByItemId(@Param("itemId") UUID itemId);

    @Query("SELECT b FROM InventoryBatchPersistenceEntity b WHERE b.item.id = :itemId AND b.remainingQty > 0 ORDER BY b.arrivalDate ASC")
    List<InventoryBatchPersistenceEntity> findAvailableBatchesFifo(@Param("itemId") UUID itemId);

    @Query("SELECT COALESCE(SUM(b.remainingQty * b.unitCost), 0) FROM InventoryBatchPersistenceEntity b WHERE b.tenantId = :tenantId AND b.remainingQty > 0")
    BigDecimal sumValuationByTenantId(@Param("tenantId") UUID tenantId);
}
