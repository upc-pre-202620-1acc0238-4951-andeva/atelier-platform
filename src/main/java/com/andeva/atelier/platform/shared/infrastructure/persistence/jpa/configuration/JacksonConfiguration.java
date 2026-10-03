package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared infrastructure configuration for JSON serialization and Jackson 2 ObjectMapper bean.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class JacksonConfiguration {

    /**
     * Provides a standard Jackson 2 ObjectMapper configured with JavaTimeModule and ISO-8601 timestamps.
     *
     * @return configured ObjectMapper bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
