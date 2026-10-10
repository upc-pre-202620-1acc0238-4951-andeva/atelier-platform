package com.andeva.atelier.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Global OpenAPI 3.0 configuration providing metadata for interactive Swagger UI
 * documentation and client contract generation across the Atelier Platform.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${spring.application.name:Atelier Platform Backend API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .version("1.0.0")
                        .description("RESTful API specification for the Atelier multi-branch automotive management SaaS platform.")
                        .contact(new Contact().name("Andeva Engineering Team").email("engineering@andeva.pe"))
                        .license(new License().name("Proprietary - Andeva Software")))
                .servers(List.of(
                        new Server().url("/").description("Default Server Context"),
                        new Server().url("https://atelier-platform.onrender.com").description("Render Production Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Authentication based on signed JWT tokens (RFC 7519). Enter 'Bearer {token}'.")));
    }
}
