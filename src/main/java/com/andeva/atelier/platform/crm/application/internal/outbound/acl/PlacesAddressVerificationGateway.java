package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

import java.util.Optional;

/**
 * Outbound ACL port for geocoding and address normalization via external provider.
 *
 * @author Adiel Sanchez Santin
 */
public interface PlacesAddressVerificationGateway {

    Optional<VerifiedAddressDto> verifyAddress(String rawAddress);
}
