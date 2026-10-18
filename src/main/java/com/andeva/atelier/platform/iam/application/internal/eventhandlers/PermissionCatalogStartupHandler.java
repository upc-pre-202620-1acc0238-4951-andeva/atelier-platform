package com.andeva.atelier.platform.iam.application.internal.eventhandlers;

import com.andeva.atelier.platform.iam.application.internal.commandservices.PermissionCatalogSeedingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Synchronizes the RBAC permission catalog and existing tenant roles once the application is ready.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class PermissionCatalogStartupHandler {

    private final PermissionCatalogSeedingService seedingService;

    public PermissionCatalogStartupHandler(PermissionCatalogSeedingService seedingService) {
        this.seedingService = Objects.requireNonNull(seedingService, "PermissionCatalogSeedingService cannot be null");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void on() {
        seedingService.synchronize();
    }
}
