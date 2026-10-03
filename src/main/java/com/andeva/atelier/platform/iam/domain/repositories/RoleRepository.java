package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for managing Role security aggregates.
 *
 * @author Joel Huamani Estefanero
 */
public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(RoleId id);

    Optional<Role> findByTenantIdAndName(TenantId tenantId, String name);

    Optional<Role> findByTenantIdAndCode(TenantId tenantId, String code);

    List<Role> findByTenantId(TenantId tenantId);

    boolean existsByTenantIdAndName(TenantId tenantId, String name);

    void delete(Role role);
}
