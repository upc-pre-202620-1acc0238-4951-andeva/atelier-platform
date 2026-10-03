package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.application.internal.dto.InvitationValidationResult;
import com.andeva.atelier.platform.iam.application.queryservices.InvitationQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.ValidateInvitationTokenQuery;
import com.andeva.atelier.platform.iam.domain.repositories.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Internal implementation of {@link InvitationQueryService}.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class InvitationQueryServiceImpl implements InvitationQueryService {

    private final InvitationRepository invitationRepository;
    private final TenantQueryService tenantQueryService;

    public InvitationQueryServiceImpl(
            InvitationRepository invitationRepository,
            TenantQueryService tenantQueryService) {
        this.invitationRepository = Objects.requireNonNull(invitationRepository, "InvitationRepository cannot be null");
        this.tenantQueryService = Objects.requireNonNull(tenantQueryService, "TenantQueryService cannot be null");
    }

    @Override
    public Optional<InvitationValidationResult> handle(ValidateInvitationTokenQuery query) {
        Objects.requireNonNull(query, "Query cannot be null");

        Optional<Invitation> invitationOptional = invitationRepository.findByToken(query.token().trim());
        if (invitationOptional.isEmpty()) {
            return Optional.empty();
        }

        Invitation invitation = invitationOptional.get();
        if (!invitation.isPending() || invitation.expiresAt().isBefore(Instant.now())) {
            return Optional.of(InvitationValidationResult.invalid("Invitation has expired or has already been accepted"));
        }

        String tenantName = tenantQueryService.handle(new GetTenantByIdQuery(invitation.tenantId()))
                .map(Tenant::name)
                .orElse("Workshop");

        return Optional.of(InvitationValidationResult.valid(invitation, tenantName));
    }
}
