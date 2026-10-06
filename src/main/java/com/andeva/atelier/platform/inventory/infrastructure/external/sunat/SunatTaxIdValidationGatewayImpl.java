package com.andeva.atelier.platform.inventory.infrastructure.external.sunat;

import com.andeva.atelier.platform.inventory.application.internal.outbound.acl.SunatTaxIdValidationGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SunatTaxIdValidationGatewayImpl implements SunatTaxIdValidationGateway {

    @Override
    public boolean isValidRuc(String taxId) {
        if (taxId == null || !taxId.matches("^\\d{11}$")) {
            return false;
        }

        // Standard Peruvian RUC algorithm validation
        int[] factors = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(taxId.charAt(i)) * factors[i];
        }

        int remainder = sum % 11;
        int checkDigit = 11 - remainder;
        if (checkDigit == 10) checkDigit = 0;
        if (checkDigit == 11) checkDigit = 1;

        return checkDigit == Character.getNumericValue(taxId.charAt(10));
    }

    @Override
    public Optional<SunatCompanyInfo> lookupCompanyInfo(String taxId) {
        if (!isValidRuc(taxId)) {
            return Optional.empty();
        }
        // In-memory mock / local fallback
        return Optional.of(new SunatCompanyInfo(
                taxId,
                "PROVEEDOR AUTOMOTRIZ S.A.C.",
                "Av. Nicolas Arriola 1234, La Victoria, Lima",
                true
        ));
    }
}
