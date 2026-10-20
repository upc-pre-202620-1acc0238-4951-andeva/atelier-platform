package com.andeva.atelier.platform.crm.application.commandservices;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory.PlatformAccount;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Reception flow distinguishing walk-in customers from people who already have a platform
 * account (US44): the latter are affiliated with their vehicles instead of re-typed.
 */
public interface CustomerAffiliationService {

    AffiliationLookup lookup(TenantId tenantId, String email);

    /**
     * Registers the platform account as a customer of the workshop and links its vehicles.
     */
    Result<Customer, ApplicationError> affiliate(TenantId tenantId, String email, String taxId, String phone);

    record AffiliationLookup(Optional<Customer> workshopCustomer, Optional<PlatformAccount> platformAccount, List<Vehicle> vehicles) {
    }
}
