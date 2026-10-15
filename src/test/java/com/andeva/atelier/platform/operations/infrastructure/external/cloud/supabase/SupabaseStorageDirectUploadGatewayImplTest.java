package com.andeva.atelier.platform.operations.infrastructure.external.cloud.supabase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Supabase Direct-to-Cloud Storage Gateway Tests")
class SupabaseStorageDirectUploadGatewayImplTest {

    private final SupabaseStorageDirectUploadGatewayImpl gateway =
            new SupabaseStorageDirectUploadGatewayImpl("https://abcd1234.supabase.co", "atelier");

    @Test
    @DisplayName("Should accept public and signed object URLs from the configured project bucket")
    void shouldAcceptProjectBucketUrls() {
        assertThat(gateway.validateStorageUrl(
                "https://abcd1234.supabase.co/storage/v1/object/public/atelier/work-orders/1/intake.jpg")).isTrue();
        assertThat(gateway.validateStorageUrl(
                "https://abcd1234.supabase.co/storage/v1/object/sign/atelier/receipts/po-1.pdf?token=xyz")).isTrue();
    }

    @Test
    @DisplayName("Should reject URLs from other hosts, buckets, schemes or malformed input")
    void shouldRejectForeignUrls() {
        assertThat(gateway.validateStorageUrl("https://evil.supabase.co/storage/v1/object/public/atelier/x.jpg")).isFalse();
        assertThat(gateway.validateStorageUrl("https://abcd1234.supabase.co/storage/v1/object/public/other/x.jpg")).isFalse();
        assertThat(gateway.validateStorageUrl("http://abcd1234.supabase.co/storage/v1/object/public/atelier/x.jpg")).isFalse();
        assertThat(gateway.validateStorageUrl("https://example.com/image.jpg")).isFalse();
        assertThat(gateway.validateStorageUrl("not a url")).isFalse();
        assertThat(gateway.validateStorageUrl(null)).isFalse();
    }
}
