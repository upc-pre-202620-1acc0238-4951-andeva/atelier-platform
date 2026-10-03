package com.andeva.atelier.platform.iam.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Inbound Anti-Corruption Layer DTO representing a physical workshop branch.
 *
 * @author Joel Huamani Estefanero
 */
public record BranchAclDto(
        UUID branchId,
        UUID tenantId,
        String name,
        String sunatCode,
        boolean active
) implements Serializable {
}
