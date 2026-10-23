package com.andeva.atelier.platform.billing.infrastructure.external.acl.iam;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.TenantAclDto;
import com.andeva.atelier.platform.iam.interfaces.acl.dto.UserAclDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Outbound Anti-Corruption Layer (ACL) adapter querying the IAM & Tenancy Bounded Context
 * via its {@link TenancyContextFacade} Open Host Service.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class IamTenantValidationAdapter implements IamTenantValidationAclPort {

    private final TenancyContextFacade tenancyContextFacade;

    public IamTenantValidationAdapter(TenancyContextFacade tenancyContextFacade) {
        this.tenancyContextFacade = Objects.requireNonNull(tenancyContextFacade, "TenancyContextFacade cannot be null");
    }

    @Override
    public java.util.List<TenantId> listTenantIds() {
        return tenancyContextFacade.fetchAllTenantIds().stream().map(TenantId::of).toList();
    }

    @Override
    public boolean tenantExists(TenantId tenantId) {
        if (tenantId == null) {
            return false;
        }
        return tenancyContextFacade.fetchTenantById(tenantId.value()).isPresent();
    }

    @Override
    public String getTenantLegalName(TenantId tenantId) {
        if (tenantId == null) {
            return "Workshop";
        }
        return tenancyContextFacade.fetchTenantById(tenantId.value())
                .map(TenantAclDto::legalName)
                .filter(name -> !name.isBlank())
                .orElse("Workshop " + tenantId.value());
    }

    /**
     * Billing contact is the workshop owner's account email; {@code null} when the tenant has no active owner.
     */
    @Override
    public String getTenantContactEmail(TenantId tenantId) {
        if (tenantId == null) {
            return null;
        }
        return tenancyContextFacade.fetchTenantOwner(tenantId.value())
                .map(UserAclDto::email)
                .orElse(null);
    }
}
