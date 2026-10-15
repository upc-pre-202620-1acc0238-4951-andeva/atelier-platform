package com.andeva.atelier.platform.crm.infrastructure.external.google;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.PlacesAddressVerificationGateway;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.VerifiedAddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Google Places API (New) adapter normalizing free-text addresses into a formatted address,
 * coordinates and a stable place ID through the Text Search endpoint, biased to Peru.
 * Without an API key (local development) it echoes the address with Lima coordinates.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class GooglePlacesClient implements PlacesAddressVerificationGateway {

    private static final Logger log = LoggerFactory.getLogger(GooglePlacesClient.class);
    private static final String TEXT_SEARCH_URL = "https://places.googleapis.com/v1/places:searchText";
    private static final String FIELD_MASK = "places.id,places.formattedAddress,places.location";

    private final String apiKey;
    private final RestClient restClient;

    public GooglePlacesClient() {
        this("");
    }

    @org.springframework.beans.factory.annotation.Autowired
    public GooglePlacesClient(@Value("${google.maps.api-key:}") String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.restClient = RestClient.builder().build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<VerifiedAddressDto> verifyAddress(String rawAddress) {
        if (rawAddress == null || rawAddress.isBlank()) {
            return Optional.empty();
        }

        if (apiKey.isEmpty()) {
            log.debug("[Mock Places] Google Maps API key not configured, echoing address: {}", rawAddress);
            return Optional.of(new VerifiedAddressDto(
                    rawAddress.trim(),
                    -12.046374,
                    -77.042793,
                    "place_" + Math.abs(rawAddress.hashCode())
            ));
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(TEXT_SEARCH_URL)
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "textQuery", rawAddress.trim(),
                            "regionCode", "PE",
                            "languageCode", "es",
                            "pageSize", 1
                    ))
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> places = response != null
                    ? (List<Map<String, Object>>) response.getOrDefault("places", List.of())
                    : List.of();
            if (places.isEmpty()) {
                return Optional.empty();
            }

            Map<String, Object> place = places.get(0);
            Map<String, Object> location = (Map<String, Object>) place.get("location");
            if (location == null) {
                return Optional.empty();
            }
            return Optional.of(new VerifiedAddressDto(
                    (String) place.get("formattedAddress"),
                    ((Number) location.get("latitude")).doubleValue(),
                    ((Number) location.get("longitude")).doubleValue(),
                    (String) place.get("id")
            ));
        } catch (Exception e) {
            log.warn("Google Places address verification failed for '{}': {}", rawAddress, e.getMessage());
            return Optional.empty();
        }
    }
}
