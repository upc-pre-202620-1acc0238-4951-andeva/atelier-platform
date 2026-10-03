package com.andeva.atelier.platform.iam.application.internal.dto;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;

import java.io.Serializable;

/**
 * Outcome of evaluating an onboarding invitation token prior to registration.
 *
 * @author Joel Huamani Estefanero
 */
public record InvitationValidationResult(
        Invitation invitation,
        String tenantName,
        boolean valid,
        String failureReason
) implements Serializable {

    public static InvitationValidationResult valid(Invitation invitation, String tenantName) {
        return new InvitationValidationResult(invitation, tenantName, true, null);
    }

    public static InvitationValidationResult invalid(String failureReason) {
        return new InvitationValidationResult(null, null, false, failureReason);
    }
}
