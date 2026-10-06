package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterSupplierResource(
        @NotBlank(message = "Business name cannot be blank")
        String businessName,

        @NotBlank(message = "Tax ID (RUC) cannot be blank")
        @Pattern(regexp = "^\\d{8,20}$", message = "Tax ID must be between 8 and 20 digits")
        String taxId,

        String contactName,
        String phone,
        String email,
        String address
) {
}
