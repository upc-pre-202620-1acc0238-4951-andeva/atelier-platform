package com.andeva.atelier.platform.inventory.infrastructure.external.storage;

import com.andeva.atelier.platform.inventory.application.internal.outbound.acl.StorageGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FirebaseStorageGatewayImpl implements StorageGateway {

    @Override
    public String generatePresignedUploadUrl(String destinationPath, String contentType) {
        String cleanPath = destinationPath != null ? destinationPath : "receipts/" + UUID.randomUUID() + ".jpg";
        return "https://storage.googleapis.com/atelier-platform-receipts/" + cleanPath;
    }
}
