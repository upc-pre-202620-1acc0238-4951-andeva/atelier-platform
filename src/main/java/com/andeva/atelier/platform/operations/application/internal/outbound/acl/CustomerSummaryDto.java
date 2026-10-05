package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.UUID;

public record CustomerSummaryDto(
        UUID id,
        String fullName,
        String taxId,
        String email,
        String phone,
        String customerType
) {
}
