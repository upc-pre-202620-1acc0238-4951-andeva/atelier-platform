package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record ReceivePurchaseOrderResource(
        @NotBlank(message = "Receipt image URL is mandatory to confirm delivery")
        String receiptImageUrl,

        @NotBlank(message = "Receipt number / Invoice number is mandatory")
        String receiptNumber,

        Instant receivedAt
) {
}
