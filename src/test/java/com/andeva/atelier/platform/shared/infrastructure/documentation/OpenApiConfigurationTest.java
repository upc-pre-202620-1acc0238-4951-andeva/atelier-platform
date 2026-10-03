package com.andeva.atelier.platform.shared.infrastructure.documentation;

import com.andeva.atelier.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link OpenApiConfiguration}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("OpenAPI 3.0 Configuration Unit Tests")
class OpenApiConfigurationTest {

    @Test
    @DisplayName("Should create OpenAPI specification with metadata and security schemes")
    void shouldCreateOpenApiSpecification() {
        OpenApiConfiguration config = new OpenApiConfiguration();
        ReflectionTestUtils.setField(config, "applicationName", "Atelier Automotive Platform API");

        OpenAPI openAPI = config.customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Atelier Automotive Platform API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("engineering@andeva.pe");
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Proprietary - Andeva Software");

        assertThat(openAPI.getServers()).hasSize(2);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("/api/v1");
        assertThat(openAPI.getServers().get(1).getUrl()).isEqualTo("https://api.atelier.andeva.pe/api/v1");

        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}
