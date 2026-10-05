package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

/**
 * DTO representing an address verified by an external geocoding provider.
 *
 * @param formattedAddress Normalized postal or commercial address
 * @param latitude         Geographic latitude
 * @param longitude        Geographic longitude
 * @param placeId          External identifier from the geocoding provider
 */
public record VerifiedAddressDto(
        String formattedAddress,
        double latitude,
        double longitude,
        String placeId
) {}
