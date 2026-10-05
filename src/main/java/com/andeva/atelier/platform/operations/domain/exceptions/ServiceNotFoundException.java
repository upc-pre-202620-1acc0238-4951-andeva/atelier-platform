package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;

import java.util.UUID;

public class ServiceNotFoundException extends OperationsDomainException {

    public ServiceNotFoundException(ServiceId serviceId) {
        super("SERVICE_NOT_FOUND", String.format("Service item with identifier %s was not found", serviceId != null ? serviceId.value() : "null"));
    }

    public ServiceNotFoundException(UUID serviceId) {
        super("SERVICE_NOT_FOUND", String.format("Service item with identifier %s was not found", serviceId));
    }
}
