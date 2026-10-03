package com.andeva.atelier.platform.iam.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Inbound Anti-Corruption Layer DTO representing a platform user account.
 *
 * @author Joel Huamani Estefanero
 */
public record UserAclDto(
        UUID userId,
        String email,
        String fullName,
        String status
) implements Serializable {
}
