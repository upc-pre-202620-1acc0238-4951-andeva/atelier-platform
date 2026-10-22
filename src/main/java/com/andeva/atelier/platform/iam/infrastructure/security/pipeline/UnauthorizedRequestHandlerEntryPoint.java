package com.andeva.atelier.platform.iam.infrastructure.security.pipeline;

import com.andeva.atelier.platform.shared.interfaces.rest.resources.ErrorResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication entry point emitting the shared ErrorResource body ({code, message, details, timestamp})
 * whenever an unauthenticated request attempts to access a protected resource.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public UnauthorizedRequestHandlerEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResource error = ErrorResource.of("UNAUTHORIZED", authException != null && authException.getMessage() != null
                ? authException.getMessage()
                : "Full authentication is required to access this resource");

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
