package com.andeva.atelier.platform.crm.application.internal.queryservices;

import com.andeva.atelier.platform.crm.application.queryservices.CustomerMembershipQueryService;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembersByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembershipsByUserIdQuery;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of CustomerMembershipQueryService executing read-only projections.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(readOnly = true)
public class CustomerMembershipQueryServiceImpl implements CustomerMembershipQueryService {

    private final CustomerMembershipRepository customerMembershipRepository;

    public CustomerMembershipQueryServiceImpl(CustomerMembershipRepository customerMembershipRepository) {
        this.customerMembershipRepository = Objects.requireNonNull(customerMembershipRepository, "CustomerMembershipRepository cannot be null");
    }

    @Override
    public List<CustomerMembership> handle(GetCustomerMembershipsByUserIdQuery query) {
        Objects.requireNonNull(query, "GetCustomerMembershipsByUserIdQuery cannot be null");
        return customerMembershipRepository.findByUserIdAndStatus(query.userId(), CustomerMembershipStatus.ACTIVE);
    }

    @Override
    public List<CustomerMembership> handle(GetCustomerMembersByCustomerIdQuery query) {
        Objects.requireNonNull(query, "GetCustomerMembersByCustomerIdQuery cannot be null");
        return customerMembershipRepository.findByCustomerId(query.customerId());
    }
}
