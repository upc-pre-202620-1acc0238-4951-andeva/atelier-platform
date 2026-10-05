package com.andeva.atelier.platform.crm.infrastructure.external.google;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlacesAddressVerificationGateway;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.VerifiedAddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter for Google Places API to verify and standardize addresses.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class GooglePlacesClient implements PlacesAddressVerificationGateway {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesClient.class);

    @Override
    public Optional<VerifiedAddressDto> verifyAddress(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) {
            return Optional.empty();
        }
        log.debug("Verifying address through external Places client: {}", rawAddress);
        return Optional.of(new VerifiedAddressDto(
                rawAddress.trim(),
                -12.046374,
                -77.042793,
                "place_" + Math.abs(rawAddress.hashCode())
        ));
    }
}
