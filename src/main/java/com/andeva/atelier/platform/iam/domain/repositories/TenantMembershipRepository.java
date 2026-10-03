package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for managing TenantMembership aggregates (staff employment contracts).
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantMembershipRepository {

    TenantMembership save(TenantMembership membership);

    Optional<TenantMembership> findById(TenantMembershipId id);

    Optional<TenantMembership> findByTenantIdAndUserId(TenantId tenantId, UserId userId);

    List<TenantMembership> findByTenantId(TenantId tenantId);

    List<TenantMembership> findByUserId(UserId userId);

    long countByTenantId(TenantId tenantId);
}
