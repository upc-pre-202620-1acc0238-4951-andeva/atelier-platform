package com.andeva.atelier.platform.iam.interfaces.acl;

import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.BranchGeofenceAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.UserAclDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Open Host Service (OHS) inbound facade exposing cross-context tenancy operations.
 * Allows other Bounded Contexts (MRO, CRM, HR, Invoicing, Billing) to safely query IAM state.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenancyContextFacade {

    /**
     * Resolves workshop tenant metadata by identifier.
     *
     * @param tenantId the UUID of the tenant
     * @return Optional containing the tenant ACL DTO, or empty if not found
     */
    Optional<TenantAclDto> fetchTenantById(UUID tenantId);

    /**
     * Resolves physical branch metadata by identifier.
     *
     * @param branchId the UUID of the branch
     * @return Optional containing the branch ACL DTO, or empty if not found
     */
    Optional<BranchAclDto> fetchBranchById(UUID branchId);

    /**
     * Resolves user account metadata by identifier.
     *
     * @param userId the UUID of the user
     * @return Optional containing the user ACL DTO, or empty if not found
     */
    Optional<UserAclDto> fetchUserById(UUID userId);

    /**
     * Resolves the user account holding the active workshop owner role in a tenant.
     *
     * @param tenantId the tenant UUID
     * @return Optional containing the owner's user ACL DTO, or empty if none is active
     */
    Optional<UserAclDto> fetchTenantOwner(UUID tenantId);

    /**
     * Resolves the push notification (FCM) token registered by the user's mobile device.
     *
     * @param userId the user UUID
     * @return Optional containing the FCM token, or empty if no device is registered
     */
    Optional<String> fetchUserDeviceToken(UUID userId);

    /**
     * Checks that a membership exists, belongs to the tenant and is active.
     *
     * @param membershipId the tenant membership UUID
     * @param tenantId the tenant UUID
     * @return true if the membership is active within the tenant
     */
    boolean isActiveMembershipOfTenant(UUID membershipId, UUID tenantId);

    /**
     * Checks if a user is an active staff member of a specific workshop tenant.
     *
     * @param userId the user UUID
     * @param tenantId the tenant UUID
     * @return true if an active membership exists, false otherwise
     */
    boolean isUserStaffMemberOfTenant(UUID userId, UUID tenantId);

    /**
     * Resolves the active staff membership of a user inside a workshop tenant.
     *
     * @param userId the user UUID
     * @param tenantId the tenant UUID
     * @return the active membership UUID, if the user works in the tenant
     */
    Optional<UUID> fetchActiveMembershipId(UUID userId, UUID tenantId);

    /**
     * Resolves the user account behind a staff membership (e.g. to reach the person's device).
     *
     * @param membershipId the tenant membership UUID
     * @return the user UUID, if the membership exists
     */
    Optional<UUID> fetchUserIdOfMembership(UUID membershipId);

    /**
     * Evaluates whether a given geographic coordinate falls within the branch's operational geofence.
     *
     * @param branchId the branch UUID
     * @param latitude latitude in WGS84
     * @param longitude longitude in WGS84
     * @return true if coordinates are inside the branch geofence, false otherwise
     */
    boolean isPointWithinBranchGeofence(UUID branchId, double latitude, double longitude);

    /**
     * Retrieves geofence boundaries for a physical branch.
     *
     * @param branchId the branch UUID
     * @return Optional containing geofence parameters
     */
    Optional<BranchGeofenceAclDto> fetchBranchGeofence(UUID branchId);
}
