package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.WorkOrderPartAllocationPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkOrderPartAllocationPersistenceRepository extends JpaRepository<WorkOrderPartAllocationPersistenceEntity, UUID> {

    @Query("SELECT a FROM WorkOrderPartAllocationPersistenceEntity a WHERE a.workOrderId = :workOrderId "
            + "AND a.taskId = :taskId AND a.itemId = :itemId AND a.quantity > a.returnedQuantity "
            + "ORDER BY a.allocatedAt DESC")
    List<WorkOrderPartAllocationPersistenceEntity> findOpenOfTask(@Param("workOrderId") UUID workOrderId,
                                                                 @Param("taskId") UUID taskId,
                                                                 @Param("itemId") UUID itemId);

    @Query("SELECT a FROM WorkOrderPartAllocationPersistenceEntity a WHERE a.workOrderId = :workOrderId "
            + "AND a.itemId = :itemId AND a.quantity > a.returnedQuantity ORDER BY a.allocatedAt DESC")
    List<WorkOrderPartAllocationPersistenceEntity> findOpenOfOrder(@Param("workOrderId") UUID workOrderId,
                                                                  @Param("itemId") UUID itemId);

    List<WorkOrderPartAllocationPersistenceEntity> findByWorkOrderIdOrderByAllocatedAtAsc(UUID workOrderId);
}
