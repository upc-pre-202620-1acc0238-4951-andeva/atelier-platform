package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound ACL port to Human Resources for the mechanic's scheduled work shift.
 */
public interface WorkforceShiftAclService {

    /**
     * Scheduled start and end of the mechanic's shift, if one is assigned.
     */
    Optional<ShiftWindow> findShift(UUID tenantId, UUID mechanicMembershipId);

    record ShiftWindow(LocalTime start, LocalTime end) {
    }
}
