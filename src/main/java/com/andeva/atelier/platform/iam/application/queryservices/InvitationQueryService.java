package com.andeva.atelier.platform.iam.application.queryservices;

import com.andeva.atelier.platform.iam.application.internal.dto.InvitationValidationResult;
import com.andeva.atelier.platform.iam.domain.model.queries.ValidateInvitationTokenQuery;

import java.util.Optional;

/**
 * Public query service interface for reading and evaluating staff invitations.
 *
 * @author Joel Huamani Estefanero
 */
public interface InvitationQueryService {

    /**
     * Evaluates whether a cryptographic onboarding token is authentic, pending, and within its validity window.
     *
     * @param query the validation query carrying the token
     * @return an optional containing the validation result if found and evaluable
     */
    Optional<InvitationValidationResult> handle(ValidateInvitationTokenQuery query);
}
