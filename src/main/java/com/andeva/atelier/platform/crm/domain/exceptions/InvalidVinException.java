package com.andeva.atelier.platform.crm.domain.exceptions;

public class InvalidVinException extends CrmDomainException {

    public InvalidVinException(String vin) {
        super("INVALID_VIN", String.format("The VIN '%s' does not conform to the ISO 3779 17-character standard", vin));
    }
}
