package com.andeva.atelier.platform.inventory.application.internal.outbound.acl;

public interface StorageGateway {

    String generatePresignedUploadUrl(String destinationPath, String contentType);
}
