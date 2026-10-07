package com.andeva.atelier.platform.invoicing.application.internal.outbound.acl;

import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound ACL Port for querying customer fiscal identification profiles
 * from CRM & Fleet Management or SUNAT web registers.
 *
 * @author Joel Huamani Estefanero
 */
public interface CustomerFiscalValidationAclService {

    /**
     * Retrieves customer legal and fiscal data from CRM.
     */
    Optional<CustomerFiscalInfo> getCustomerFiscalData(UUID customerId);

    /**
     * Validates active and registered tax document status against SUNAT.
     */
    boolean validateTaxIdStatus(String taxId);
}
