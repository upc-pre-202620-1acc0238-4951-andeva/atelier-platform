package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskImagePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkOrderTaskImagePersistenceRepository extends JpaRepository<WorkOrderTaskImagePersistenceEntity, UUID> {

    List<WorkOrderTaskImagePersistenceEntity> findAllByTaskIdOrderByUploadedAtDesc(UUID taskId);
}
