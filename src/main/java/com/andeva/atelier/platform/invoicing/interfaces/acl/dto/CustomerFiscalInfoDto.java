package com.andeva.atelier.platform.invoicing.interfaces.acl.dto;

/**
 * Customer fiscal identity DTO exchanged across bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerFiscalInfoDto(
        String taxId,
        String legalName,
        String fiscalAddress,
        String documentType
) {}
