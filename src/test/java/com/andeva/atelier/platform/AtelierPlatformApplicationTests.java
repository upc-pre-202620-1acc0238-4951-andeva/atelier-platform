package com.andeva.atelier.platform;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test verifying that the Spring application context loads successfully.
 * Disabled during Application layer development until Infrastructure layer provides JPA repositories and security adapters.
 *
 * @author Joel Huamani Estefanero
 */
@SpringBootTest
class AtelierPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
