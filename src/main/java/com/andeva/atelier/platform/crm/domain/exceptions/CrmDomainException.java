package com.andeva.atelier.platform.crm.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Base abstract domain exception for all Customer and Fleet Management (CRM) domain invariants.
 *
 * @author Adiel Sanchez Santin
 */
public abstract class CrmDomainException extends DomainException {

    protected CrmDomainException(String code, String message) {
        super(code, message);
    }
}
