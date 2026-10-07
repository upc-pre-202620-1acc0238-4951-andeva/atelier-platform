package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.invoicing.domain.model.enums.DocumentType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Legal and fiscal identity of the voucher recipient under SUNAT regulations.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerFiscalInfo(
        TaxId taxId,
        String legalName,
        String fiscalAddress,
        DocumentType documentType
) implements Serializable {

    public CustomerFiscalInfo {
        legalName = legalName != null ? legalName.trim() : "";
        fiscalAddress = fiscalAddress != null ? fiscalAddress.trim() : "";
    }

    public static CustomerFiscalInfo of(TaxId taxId, String legalName, String fiscalAddress, DocumentType documentType) {
        return new CustomerFiscalInfo(taxId, legalName, fiscalAddress, documentType);
    }

    public static CustomerFiscalInfo anonymous() {
        return new CustomerFiscalInfo(null, "CLIENTES VARIOS", "-", DocumentType.DNI);
    }
}
