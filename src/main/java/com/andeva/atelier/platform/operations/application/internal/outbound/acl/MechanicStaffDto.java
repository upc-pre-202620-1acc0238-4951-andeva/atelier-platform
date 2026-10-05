package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.UUID;

public record MechanicStaffDto(
        UUID membershipId,
        UUID userId,
        String fullName,
        String email,
        String specialtyRole,
        boolean active
) {
}
