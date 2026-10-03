package com.andeva.atelier.platform.crm.application.queryservices;

import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembersByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembershipsByUserIdQuery;

import java.util.List;

/**
 * Public application port for corporate fleet membership queries.
 *
 * @author Adiel Sanchez Santin
 */
public interface CustomerMembershipQueryService {

    List<CustomerMembership> handle(GetCustomerMembershipsByUserIdQuery query);

    List<CustomerMembership> handle(GetCustomerMembersByCustomerIdQuery query);
}
