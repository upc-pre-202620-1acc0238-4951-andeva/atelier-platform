package com.andeva.atelier.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global OpenAPI 3.0 configuration for the Atelier Platform.
 * <p>
 * Servers are intentionally not declared: springdoc derives the server URL from the incoming
 * request (honoring {@code X-Forwarded-*} headers), so the same build works locally, on the
 * default Render host, or behind any custom domain without code changes.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class OpenApiConfiguration {

    static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Atelier Platform API")
                        .version("1.0.0")
                        .description("RESTful API specification for the Atelier multi-branch automotive management SaaS platform."))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
