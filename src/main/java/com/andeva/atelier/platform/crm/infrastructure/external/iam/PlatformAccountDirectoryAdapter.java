package com.andeva.atelier.platform.crm.infrastructure.external.iam;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlatformAccountDirectory;
import com.andeva.atelier.platform.iam.interfaces.acl.TenancyContextFacade;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PlatformAccountDirectoryAdapter implements PlatformAccountDirectory {

    private final TenancyContextFacade tenancyContextFacade;

    public PlatformAccountDirectoryAdapter(TenancyContextFacade tenancyContextFacade) {
        this.tenancyContextFacade = Objects.requireNonNull(tenancyContextFacade, "tenancyContextFacade cannot be null");
    }

    @Override
    public Optional<PlatformAccount> findByEmail(String email) {
        return tenancyContextFacade.fetchUserByEmail(email)
                .filter(user -> "ACTIVE".equals(user.status()))
                .map(user -> new PlatformAccount(user.userId(), user.email(), user.fullName()));
    }
}
