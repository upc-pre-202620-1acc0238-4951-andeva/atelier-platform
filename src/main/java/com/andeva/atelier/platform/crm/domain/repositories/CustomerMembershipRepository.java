package com.andeva.atelier.platform.crm.domain.repositories;

import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for corporate fleet memberships.
 */
public interface CustomerMembershipRepository {

    CustomerMembership save(CustomerMembership membership);

    Optional<CustomerMembership> findById(CustomerMembershipId id);

    Optional<CustomerMembership> findByCustomerIdAndUserId(CustomerId customerId, UserId userId);

    List<CustomerMembership> findByCustomerId(CustomerId customerId);

    List<CustomerMembership> findByUserIdAndStatus(UserId userId, CustomerMembershipStatus status);

    boolean existsByCustomerIdAndUserIdAndStatus(CustomerId customerId, UserId userId, CustomerMembershipStatus status);
}
