package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByTaxIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Transactional read-only query service implementation for workshop Tenant aggregates.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class TenantQueryServiceImpl implements TenantQueryService {

    private final TenantRepository tenantRepository;

    public TenantQueryServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository cannot be null");
    }

    @Override
    public Optional<Tenant> handle(GetTenantByIdQuery query) {
        Objects.requireNonNull(query, "GetTenantByIdQuery cannot be null");
        return tenantRepository.findById(query.tenantId());
    }

    @Override
    public Optional<Tenant> handle(GetTenantByTaxIdQuery query) {
        Objects.requireNonNull(query, "GetTenantByTaxIdQuery cannot be null");
        return tenantRepository.findByTaxId(query.taxId());
    }
}
