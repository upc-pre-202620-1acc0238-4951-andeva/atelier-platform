package com.andeva.atelier.platform.operations.infrastructure.external.cloud.firebase;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.StorageMetadataDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FirebaseStorageDirectUploadGatewayImpl implements DirectToCloudStorageGateway {

    private static final String FIREBASE_STORAGE_DOMAIN = "firebasestorage.googleapis.com";
    private static final String GCS_STORAGE_DOMAIN = "storage.googleapis.com";
    private static final String SUPABASE_STORAGE_DOMAIN = "supabase.co";
    private static final String CLOUDINARY_STORAGE_DOMAIN = "cloudinary.com";

    @Override
    public boolean validateStorageUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        return rawUrl.startsWith("https://") &&
                (rawUrl.contains(FIREBASE_STORAGE_DOMAIN) ||
                 rawUrl.contains(GCS_STORAGE_DOMAIN) ||
                 rawUrl.contains(SUPABASE_STORAGE_DOMAIN) ||
                 rawUrl.contains(CLOUDINARY_STORAGE_DOMAIN));
    }

    @Override
    public Optional<StorageMetadataDto> fetchMetadata(String rawUrl) {
        if (!validateStorageUrl(rawUrl)) {
            return Optional.empty();
        }
        return Optional.of(new StorageMetadataDto(
                rawUrl,
                "image/jpeg",
                1024L * 500,
                "sha256-dummy-checksum",
                rawUrl
        ));
    }
}
