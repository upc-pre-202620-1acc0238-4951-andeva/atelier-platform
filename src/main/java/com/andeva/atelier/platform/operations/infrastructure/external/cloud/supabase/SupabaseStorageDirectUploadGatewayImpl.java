package com.andeva.atelier.platform.operations.infrastructure.external.cloud.supabase;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.DirectToCloudStorageGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Objects;

/**
 * Supabase Storage adapter for the Direct-to-Cloud pattern. Only HTTPS object URLs served by the
 * configured Supabase project and bucket are accepted (public or signed), so clients cannot attach
 * arbitrary external links as work order evidence.
 */
@Component
public class SupabaseStorageDirectUploadGatewayImpl implements DirectToCloudStorageGateway {

    private final String projectHost;
    private final String bucketPathSegment;

    public SupabaseStorageDirectUploadGatewayImpl(
            @Value("${app.storage.supabase.url:https://localhost.supabase.co}") String supabaseUrl,
            @Value("${app.storage.supabase.bucket:atelier}") String bucket
    ) {
        this.projectHost = URI.create(Objects.requireNonNull(supabaseUrl, "Supabase URL cannot be null").trim()).getHost();
        this.bucketPathSegment = "/" + Objects.requireNonNull(bucket, "Supabase bucket cannot be null").trim() + "/";
    }

    @Override
    public boolean validateStorageUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            String path = uri.getPath();
            return "https".equalsIgnoreCase(uri.getScheme())
                    && projectHost != null && projectHost.equalsIgnoreCase(uri.getHost())
                    && path != null
                    && path.startsWith("/storage/v1/object/")
                    && path.contains(bucketPathSegment);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
