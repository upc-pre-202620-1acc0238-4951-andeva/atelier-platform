package com.andeva.atelier.platform.inventory.interfaces.rest.resources.requests;

public record UpdateSupplierResource(
        String businessName,
        String contactName,
        String phone,
        String email,
        String address,
        Boolean active
) {
}
