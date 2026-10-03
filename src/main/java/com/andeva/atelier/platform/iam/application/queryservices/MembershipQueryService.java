package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByTenantAndUserQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipsByTenantIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public query service interface for reading TenantMembership staff records.
 *
 * @author Joel Huamani Estefanero
 */
public interface MembershipQueryService {

    Optional<TenantMembership> handle(GetMembershipByIdQuery query);

    List<TenantMembership> handle(GetMembershipsByTenantIdQuery query);

    Optional<TenantMembership> handle(GetMembershipByTenantAndUserQuery query);
}
