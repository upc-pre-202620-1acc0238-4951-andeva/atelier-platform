package com.andeva.atelier.platform.operations.domain.exceptions;

import java.math.BigDecimal;

public class InvalidLaborHoursException extends OperationsDomainException {

    public InvalidLaborHoursException(BigDecimal hours) {
        super("INVALID_LABOR_HOURS", String.format("Labor hours value must be non-negative and properly scaled: %s", hours));
    }
}
