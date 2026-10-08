package com.andeva.atelier.platform.invoicing.infrastructure.external.acl.crm;

import com.andeva.atelier.platform.crm.interfaces.acl.CustomerFleetContextFacade;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final CustomerFleetContextFacade customerFleetContextFacade;

    public CustomerFiscalValidationAclAdapter() {
        this(null);
    }

    @Autowired(required = false)
    public CustomerFiscalValidationAclAdapter(CustomerFleetContextFacade customerFleetContextFacade) {
        this.customerFleetContextFacade = customerFleetContextFacade;
    }

    @Override
    public Optional<CustomerFiscalInfo> getCustomerFiscalData(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }

        log.debug("Resolving customer fiscal info for customer ID: {}", customerId);

        if (customerFleetContextFacade != null) {
            try {
                Optional<CustomerAclDto> optDto = customerFleetContextFacade.fetchCustomerById(customerId);
                if (optDto.isPresent()) {
                    CustomerAclDto dto = optDto.get();
                    String rawTaxId = dto.taxId();
                    String type = dto.type();
                    
                    DocumentType docType;
                    TaxId taxIdObj = null;
                    try {
                        if (rawTaxId != null && !rawTaxId.isBlank()) {
                            taxIdObj = TaxId.of(rawTaxId);
                        }
                        docType = "COMPANY".equalsIgnoreCase(type) ? DocumentType.RUC : DocumentType.DNI;
                    } catch (Exception e) {
                        log.debug("TaxId '{}' from CRM for customer {} could not be validated: {}", rawTaxId, customerId, e.getMessage());
                        docType = "COMPANY".equalsIgnoreCase(type) ? DocumentType.RUC : DocumentType.DNI;
                    }
                    return Optional.of(CustomerFiscalInfo.of(taxIdObj, dto.displayName(), "-", docType));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch customer from CRM facade, falling back to anonymous profile", e);
            }
        }

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
