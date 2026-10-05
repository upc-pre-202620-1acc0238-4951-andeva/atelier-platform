package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.UUID;

public record VehicleSummaryDto(
        UUID id,
        String plate,
        String vin,
        String brand,
        String model,
        Integer year,
        UUID ownerCustomerId
) {
}
