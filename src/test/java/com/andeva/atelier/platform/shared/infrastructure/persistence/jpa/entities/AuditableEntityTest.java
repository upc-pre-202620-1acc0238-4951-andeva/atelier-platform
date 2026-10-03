package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for {@link AuditableAbstractPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Auditable Abstract Persistence Entity Unit Tests")
class AuditableEntityTest {

    static class SampleEntity extends AuditableAbstractPersistenceEntity {
        public SampleEntity() {
            super();
        }

        public SampleEntity(UUID id) {
            super(id);
        }
    }

    static class AnotherEntity extends AuditableAbstractPersistenceEntity {
        public AnotherEntity(UUID id) {
            super(id);
        }
    }

    @Test
    @DisplayName("Should initialize fields and verify getters and setters")
    void shouldInitializeFieldsAndVerifyAccessors() {
        SampleEntity entity = new SampleEntity();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        entity.setId(id);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now.plusSeconds(60));

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now.plusSeconds(60));
    }

    @Test
    @DisplayName("Should verify equality and hashcode based on ID and type")
    void shouldVerifyEqualityAndHashCode() {
        UUID sharedId = UUID.randomUUID();
        SampleEntity entity1 = new SampleEntity(sharedId);
        SampleEntity entity2 = new SampleEntity(sharedId);
        SampleEntity entity3 = new SampleEntity(UUID.randomUUID());
        AnotherEntity differentTypeEntity = new AnotherEntity(sharedId);

        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        assertThat(entity1).isNotEqualTo(entity3);
        assertThat(entity1).isNotEqualTo(differentTypeEntity);
        assertThat(entity1).isNotEqualTo(null);
        assertThat(entity1).isNotEqualTo(new Object());

        SampleEntity unpersisted1 = new SampleEntity();
        SampleEntity unpersisted2 = new SampleEntity();
        assertThat(unpersisted1).isNotEqualTo(unpersisted2);
    }
}
