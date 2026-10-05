package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.util.UUID;

public record WorkBayResource(
        UUID id,
        UUID tenantId,
        UUID branchId,
        String name,
        String bayType,
        String status,
        UUID currentWorkOrderId
) {}
