package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByTaxIdQuery;

import java.util.Optional;

/**
 * Public query service interface for reading workshop Tenant aggregate state.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantQueryService {

    Optional<Tenant> handle(GetTenantByIdQuery query);

    Optional<Tenant> handle(GetTenantByTaxIdQuery query);
}
