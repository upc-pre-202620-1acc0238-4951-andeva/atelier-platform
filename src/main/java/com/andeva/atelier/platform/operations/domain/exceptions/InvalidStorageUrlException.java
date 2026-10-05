package com.andeva.atelier.platform.operations.domain.exceptions;

public class InvalidStorageUrlException extends OperationsDomainException {

    public InvalidStorageUrlException(String url) {
        super("INVALID_STORAGE_URL", String.format("Evidence storage URL is invalid or does not satisfy HTTPS cloud protocol: %s", url));
    }
}
