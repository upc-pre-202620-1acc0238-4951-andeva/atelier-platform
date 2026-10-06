package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import java.util.UUID;

public record ResolveAlertResource(
        UUID purchaseOrderId,
        String resolutionNotes
) {
}
