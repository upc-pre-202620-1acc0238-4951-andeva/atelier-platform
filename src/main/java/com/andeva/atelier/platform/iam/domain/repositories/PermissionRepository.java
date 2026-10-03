package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for atomic Permission catalog entities.
 *
 * @author Joel Huamani Estefanero
 */
public interface PermissionRepository {

    List<Permission> findAll();

    List<Permission> findByIdIn(Collection<PermissionId> ids);

    Optional<Permission> findByName(String name);

    Permission save(Permission permission);
}
