package com.andeva.atelier.platform.operations.domain.exceptions;

public class InvalidMileageException extends OperationsDomainException {

    public InvalidMileageException(Integer value) {
        super("INVALID_MILEAGE", String.format("Vehicle reception mileage cannot be negative or invalid: %s", value));
    }
}
