package com.andeva.atelier.platform.shared.infrastructure.documentation;

import com.andeva.atelier.platform.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link OpenApiConfiguration}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("OpenAPI 3.0 Configuration Unit Tests")
class OpenApiConfigurationTest {

    @Test
    @DisplayName("Should create OpenAPI specification with metadata and bearer security scheme")
    void shouldCreateOpenApiSpecification() {
        OpenAPI openAPI = new OpenApiConfiguration().customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Atelier Platform API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");

        assertThat(openAPI.getSecurity()).hasSize(1);
        assertThat(openAPI.getSecurity().get(0)).containsKey("bearerAuth");

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("Should not hardcode servers so springdoc infers them from the request host")
    void shouldNotDeclareHardcodedServers() {
        OpenAPI openAPI = new OpenApiConfiguration().customOpenAPI();

        assertThat(openAPI.getServers()).isNullOrEmpty();
    }
}
