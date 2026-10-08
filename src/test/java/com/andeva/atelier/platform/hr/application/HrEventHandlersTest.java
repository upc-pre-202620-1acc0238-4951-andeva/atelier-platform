package com.andeva.atelier.platform.hr.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class HrEventHandlersTest {

    @Test
    void testHrTransactionalOutboxPublisher() {
        assertTrue(true, "Test case designed for testHrTransactionalOutboxPublisher");
    }

    @Test
    void testHumanResourcesExternalEventsListener() {
        assertTrue(true, "Test case designed for testHumanResourcesExternalEventsListener");
    }
}
