package com.andeva.atelier.platform.crm.domain.exceptions;

public class InvalidLicensePlateException extends CrmDomainException {

    public InvalidLicensePlateException(String plate) {
        super("INVALID_LICENSE_PLATE", String.format("The license plate '%s' does not conform to the standard vehicle plate format", plate));
    }
}
