package com.andeva.atelier.platform.invoicing.application.internal.outbound.acl;

import java.util.UUID;

/**
 * Outbound ACL Port for securely storing and retrieving CDR (Constancia de Recepción)
 * and OASIS UBL 2.1 signed XML files in cloud object storage (Firebase Storage).
 *
 * @author Joel Huamani Estefanero
 */
public interface SunatCdrStorageGateway {

    /**
     * Stores CDR XML in cloud storage, returning the public access URL.
     */
    String storeCdrXml(UUID voucherId, byte[] cdrContent);

    /**
     * Stores signed UBL 2.1 XML in cloud storage, returning the public access URL.
     */
    String storeSignedXml(UUID voucherId, byte[] xmlContent);

    /**
     * Retrieves stored CDR XML payload.
     */
    byte[] retrieveCdrXml(UUID voucherId);
}
