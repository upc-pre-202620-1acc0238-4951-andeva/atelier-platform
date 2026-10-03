package com.andeva.atelier.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test verifying that the full Spring application context loads successfully
 * across all bounded contexts (Shared, IAM, Billing).
 *
 * @author Joel Huamani Estefanero
 */
@SpringBootTest
class AtelierPlatformApplicationTests {

    @Test
    void contextLoads() {
    }

}
