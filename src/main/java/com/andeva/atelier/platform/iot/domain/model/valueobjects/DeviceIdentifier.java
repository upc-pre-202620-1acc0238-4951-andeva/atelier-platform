package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import com.andeva.atelier.platform.iot.domain.exceptions.InvalidDeviceIdentifierException;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal hardware identifier for an OBD-II scanner device.
 * Enforces strict validation for either standard IEEE 802 MAC Address (Bluetooth/WiFi)
 * or 15-digit 3GPP IMEI (Cellular Telematics Modem).
 *
 * @author Joel Huamani Estefanero
 */
public record DeviceIdentifier(String value) implements Serializable {

    private static final Pattern MAC_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    private static final Pattern IMEI_PATTERN = Pattern.compile("^[0-9]{15}$");

    public DeviceIdentifier {
        Objects.requireNonNull(value, "Device identifier cannot be null");
        String trimmed = value.trim();
        if (!MAC_PATTERN.matcher(trimmed).matches() && !IMEI_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidDeviceIdentifierException(trimmed);
        }
        value = trimmed.toUpperCase(Locale.ROOT);
    }

    public static DeviceIdentifier of(String value) {
        return new DeviceIdentifier(value);
    }

    public boolean isMacAddress() {
        return MAC_PATTERN.matcher(value).matches();
    }

    public boolean isImei() {
        return IMEI_PATTERN.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
