package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.util.Optional;

public interface DirectToCloudStorageGateway {

    boolean validateStorageUrl(String rawUrl);

    Optional<StorageMetadataDto> fetchMetadata(String rawUrl);
}
