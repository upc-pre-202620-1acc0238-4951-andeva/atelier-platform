package com.andeva.atelier.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootTest(properties = "spring.profiles.active=dev")
class PropertySourceTest {
    @Autowired
    private ConfigurableEnvironment env;

    @Test
    void printPropertySources() {
        System.out.println("=== LOADED PROPERTY SOURCES ===");
        env.getPropertySources().forEach(ps -> {
            if (ps.getName().contains("Config resource")) {
                System.out.println("SOURCE: " + ps.getName());
            }
        });
    }
}
