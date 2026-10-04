package com.andeva.atelier.platform.crm.domain.exceptions;

public class InvalidMileageException extends CrmDomainException {
    public InvalidMileageException(int currentMileage, int newMileage) {
        super("INVALID_MILEAGE", String.format("La lectura ingresada (%d km) es inferior al ultimo kilometraje verificado (%d km)", newMileage, currentMileage));
    }
}
