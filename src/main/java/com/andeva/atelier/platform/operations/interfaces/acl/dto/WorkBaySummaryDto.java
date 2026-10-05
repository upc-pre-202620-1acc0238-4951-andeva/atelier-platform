package com.andeva.atelier.platform.operations.interfaces.acl.dto;

import java.util.UUID;

public record WorkBaySummaryDto(
        UUID id,
        UUID branchId,
        String name,
        String bayType,
        String status,
        UUID currentWorkOrderId
) {}
