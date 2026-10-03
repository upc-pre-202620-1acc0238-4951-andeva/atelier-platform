package com.andeva.atelier.platform.iam.domain.repositories;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Optional;

/**
 * Domain repository port for managing automotive workshop Tenant aggregates.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantRepository {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(TenantId id);

    Optional<Tenant> findByTaxId(TaxId taxId);

    boolean existsByTaxId(TaxId taxId);
}
