package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for physical workshop Branch entities.
 *
 * @author Joel Huamani Estefanero
 */
public interface BranchRepository {

    Branch save(Branch branch);

    Optional<Branch> findById(BranchId id);

    List<Branch> findByTenantId(TenantId tenantId);
}
