package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.TaskProposalPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskProposalPersistenceRepository extends JpaRepository<TaskProposalPersistenceEntity, UUID> {

    List<TaskProposalPersistenceEntity> findAllByWorkOrderId(UUID workOrderId);

    List<TaskProposalPersistenceEntity> findAllByWorkOrderIdAndStatus(UUID workOrderId, ProposalStatus status);

    @Query("SELECT p FROM TaskProposalPersistenceEntity p " +
           "WHERE p.workOrder.tenantId = :tenantId AND p.status = com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus.PENDING_REVIEW")
    List<TaskProposalPersistenceEntity> findAllPendingReviewByTenantId(@Param("tenantId") UUID tenantId);
}
