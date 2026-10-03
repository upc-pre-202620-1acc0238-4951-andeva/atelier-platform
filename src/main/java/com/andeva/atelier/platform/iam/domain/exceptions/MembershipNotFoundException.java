package com.andeva.atelier.platform.iam.domain.exceptions;

import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

/**
 * Thrown when a staff membership contract cannot be located.
 *
 * @author Joel Huamani Estefanero
 */
public class MembershipNotFoundException extends IamDomainException {

    public MembershipNotFoundException(TenantMembershipId membershipId) {
        super("MEMBERSHIP_NOT_FOUND", "Staff membership not found with identifier: " + (membershipId != null ? membershipId.value() : "null"));
    }

    public MembershipNotFoundException(TenantId tenantId, UserId userId) {
        super("MEMBERSHIP_NOT_FOUND", "Staff membership not found for tenant " + (tenantId != null ? tenantId.value() : "null")
                + " and user " + (userId != null ? userId.value() : "null"));
    }

    public MembershipNotFoundException(String message) {
        super("MEMBERSHIP_NOT_FOUND", message);
    }
}
