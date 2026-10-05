package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerMembershipPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerMembershipPersistenceRepository extends JpaRepository<CustomerMembershipPersistenceEntity, UUID> {

    Optional<CustomerMembershipPersistenceEntity> findByCustomerIdAndUserId(UUID customerId, UUID userId);

    List<CustomerMembershipPersistenceEntity> findAllByCustomerId(UUID customerId);

    List<CustomerMembershipPersistenceEntity> findAllByUserIdAndStatus(UUID userId, CustomerMembershipStatus status);

    boolean existsByCustomerIdAndUserIdAndStatus(UUID customerId, UUID userId, CustomerMembershipStatus status);
}
