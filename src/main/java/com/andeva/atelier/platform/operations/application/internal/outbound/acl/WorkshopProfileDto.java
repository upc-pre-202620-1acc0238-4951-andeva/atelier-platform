package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

/**
 * Fiscal identity of the workshop printed on customer documents.
 */
public record WorkshopProfileDto(
        String name,
        String legalName,
        String taxId
) {
}
