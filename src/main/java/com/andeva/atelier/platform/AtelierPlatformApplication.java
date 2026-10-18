package com.andeva.atelier.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot entry point for the Atelier Platform application.
 *
 * @author Joel Huamani Estefanero
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableJpaAuditing
public class AtelierPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtelierPlatformApplication.class, args);
    }

}
