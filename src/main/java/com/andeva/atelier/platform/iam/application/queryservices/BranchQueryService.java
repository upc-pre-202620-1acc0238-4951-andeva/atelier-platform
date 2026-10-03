package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public query service interface for reading physical Branch state.
 *
 * @author Joel Huamani Estefanero
 */
public interface BranchQueryService {

    Optional<Branch> handle(GetBranchByIdQuery query);

    List<Branch> handle(GetBranchesByTenantIdQuery query);
}
