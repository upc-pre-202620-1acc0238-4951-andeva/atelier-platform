package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port to IAM for platform accounts (e.g. drivers registered in the mobile app).
 */
public interface PlatformAccountDirectory {

    Optional<PlatformAccount> findByEmail(String email);

    record PlatformAccount(UUID userId, String email, String fullName) {
    }
}
