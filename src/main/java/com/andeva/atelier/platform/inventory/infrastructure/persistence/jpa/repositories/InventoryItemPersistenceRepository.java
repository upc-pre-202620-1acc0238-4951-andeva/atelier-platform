package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.InventoryItemPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemPersistenceRepository extends JpaRepository<InventoryItemPersistenceEntity, UUID> {

    Optional<InventoryItemPersistenceEntity> findByTenantIdAndSku(UUID tenantId, String sku);

    boolean existsByTenantIdAndSku(UUID tenantId, String sku);

    List<InventoryItemPersistenceEntity> findByTenantId(UUID tenantId);

    List<InventoryItemPersistenceEntity> findByTenantIdAndCategory(UUID tenantId, ItemCategory category);

    @Query("SELECT i FROM InventoryItemPersistenceEntity i WHERE i.tenantId = :tenantId AND i.totalStock <= i.minimumStock AND i.deletedAt IS NULL")
    List<InventoryItemPersistenceEntity> findLowStockItems(@Param("tenantId") UUID tenantId);

    @Query("SELECT i FROM InventoryItemPersistenceEntity i WHERE i.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItemPersistenceEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT i FROM InventoryItemPersistenceEntity i WHERE i.tenantId = :tenantId " +
            "AND (:category IS NULL OR i.category = :category) " +
            "AND (:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND i.deletedAt IS NULL")
    Page<InventoryItemPersistenceEntity> findPaged(@Param("tenantId") UUID tenantId,
                                                   @Param("category") ItemCategory category,
                                                   @Param("search") String search,
                                                   Pageable pageable);
}
