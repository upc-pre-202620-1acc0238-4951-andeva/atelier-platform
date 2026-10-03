package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.queryservices.MembershipQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipByTenantAndUserQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetMembershipsByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Transactional read-only query service implementation for TenantMembership staff records.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class MembershipQueryServiceImpl implements MembershipQueryService {

    private final TenantMembershipRepository membershipRepository;

    public MembershipQueryServiceImpl(TenantMembershipRepository membershipRepository) {
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
    }

    @Override
    public Optional<TenantMembership> handle(GetMembershipByIdQuery query) {
        Objects.requireNonNull(query, "GetMembershipByIdQuery cannot be null");
        return membershipRepository.findById(query.membershipId());
    }

    @Override
    public List<TenantMembership> handle(GetMembershipsByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetMembershipsByTenantIdQuery cannot be null");
        return membershipRepository.findByTenantId(query.tenantId());
    }

    @Override
    public Optional<TenantMembership> handle(GetMembershipByTenantAndUserQuery query) {
        Objects.requireNonNull(query, "GetMembershipByTenantAndUserQuery cannot be null");
        return membershipRepository.findByTenantIdAndUserId(query.tenantId(), query.userId());
    }
}
