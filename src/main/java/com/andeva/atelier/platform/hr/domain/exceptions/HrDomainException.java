package com.andeva.atelier.platform.hr.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

public class HrDomainException extends DomainException {

    public HrDomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    public HrDomainException(String message) {
        super("HR_DOMAIN_ERROR", message);
    }
}
