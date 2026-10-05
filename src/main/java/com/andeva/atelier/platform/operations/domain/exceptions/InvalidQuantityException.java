package com.andeva.atelier.platform.operations.domain.exceptions;

import java.math.BigDecimal;

public class InvalidQuantityException extends OperationsDomainException {

    public InvalidQuantityException(BigDecimal quantity) {
        super("INVALID_QUANTITY", String.format("Task product demand quantity must be strictly positive: %s", quantity));
    }
}
