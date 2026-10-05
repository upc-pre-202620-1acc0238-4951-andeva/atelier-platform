package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskProductPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderTaskProductPersistenceRepository extends JpaRepository<WorkOrderTaskProductPersistenceEntity, UUID> {

    List<WorkOrderTaskProductPersistenceEntity> findAllByTaskId(UUID taskId);

    @Query("SELECT p FROM WorkOrderTaskProductPersistenceEntity p " +
           "WHERE p.task.workOrder.id = :workOrderId")
    List<WorkOrderTaskProductPersistenceEntity> findAllByWorkOrderId(@Param("workOrderId") UUID workOrderId);
}
