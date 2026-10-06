package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.EmployeeProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfilePersistenceRepository extends JpaRepository<EmployeeProfilePersistenceEntity, UUID> {
    Optional<EmployeeProfilePersistenceEntity> findByMembershipId(UUID membershipId);
    List<EmployeeProfilePersistenceEntity> findAllByBranchId(UUID branchId);
    List<EmployeeProfilePersistenceEntity> findAllByTenantIdAndEmploymentStatus(UUID tenantId, EmploymentStatus status);
    boolean existsByMembershipId(UUID membershipId);
}
