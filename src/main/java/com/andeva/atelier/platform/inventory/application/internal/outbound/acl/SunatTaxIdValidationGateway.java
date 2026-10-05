package com.andeva.atelier.platform.inventory.application.internal.outbound.acl;

import java.util.Optional;

public interface SunatTaxIdValidationGateway {

    boolean isValidRuc(String taxId);

    Optional<SunatCompanyInfo> lookupCompanyInfo(String taxId);

    record SunatCompanyInfo(
            String taxId,
            String businessName,
            String address,
            boolean active
    ) {
    }
}
