package com.andeva.atelier.platform.crm.domain.exceptions;

public class AppointmentInvalidStateTransitionException extends CrmDomainException {

    public AppointmentInvalidStateTransitionException(String currentState, String targetState) {
        super("APPOINTMENT_INVALID_STATE_TRANSITION", String.format("Cannot transition appointment from state '%s' to '%s'", currentState, targetState));
    }
}
