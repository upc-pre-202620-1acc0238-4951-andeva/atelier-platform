package com.andeva.atelier.platform.invoicing.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-Corruption Layer Adapter implementing {@link CustomerFiscalValidationAclService}
 * for translating CRM customer profiles into valid fiscal structures and validating RUC/DNI.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class CustomerFiscalValidationAclAdapter implements CustomerFiscalValidationAclService {

    private static final Logger log = LoggerFactory.getLogger(CustomerFiscalValidationAclAdapter.class);

    @Override
    public Optional<CustomerFiscalInfo> getCustomerFiscalData(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }

        log.debug("Resolving customer fiscal info for customer ID: {}", customerId);

        // In a monolith, this calls CRM facade if present. Fallback provides anonymous profile
        return Optional.of(CustomerFiscalInfo.anonymous());
    }

    @Override
    public boolean validateTaxIdStatus(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            return false;
        }

        String cleaned = taxId.trim();

        // 11 digits starting with 10, 15, 17, 20 is RUC
        if (cleaned.matches("^(10|15|17|20)\\d{9}$")) {
            return true;
        }

        // 8 digits is DNI
        return cleaned.matches("^\\d{8}$");
    }
}
