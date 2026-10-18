package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

/**
 * Outbound ACL port for the Direct-to-Cloud upload pattern: mobile apps upload evidence images
 * straight to the object storage bucket and the backend only accepts URLs pointing to it.
 */
public interface DirectToCloudStorageGateway {

    boolean validateStorageUrl(String rawUrl);
}
