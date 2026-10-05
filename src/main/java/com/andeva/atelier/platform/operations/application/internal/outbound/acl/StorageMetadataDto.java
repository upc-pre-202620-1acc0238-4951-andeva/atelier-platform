package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

public record StorageMetadataDto(
        String storagePath,
        String contentType,
        long sizeBytes,
        String sha256Checksum,
        String publicHttpsUrl
) {
}
