package com.andeva.atelier.platform.shared.infrastructure.persistence.jpa.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Idempotent schema fixes that {@code ddl-auto=update} cannot apply by itself. Hibernate generated
 * CHECK constraints listing the enum values that existed when a table was first created and never
 * refreshes them, so adding an enum constant breaks inserts on existing databases. Those stale
 * constraints are dropped here; the domain enums remain the source of truth for allowed values.
 */
@Component
public class SchemaCompatibilityPatcher {

    private static final Logger log = LoggerFactory.getLogger(SchemaCompatibilityPatcher.class);

    private static final List<String> PATCHES = List.of(
            // HoldReason gained BREAK, END_OF_SHIFT and DEFECTIVE_PART
            "ALTER TABLE IF EXISTS work_order_tasks DROP CONSTRAINT IF EXISTS work_order_tasks_hold_reason_check",
            // Coolant temperature became optional for vehicles that do not answer PID 0105
            "ALTER TABLE IF EXISTS telemetry_logs ALTER COLUMN engine_temp_c DROP NOT NULL"
    );

    private final JdbcTemplate jdbcTemplate;

    public SchemaCompatibilityPatcher(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void apply() {
        for (String patch : PATCHES) {
            try {
                jdbcTemplate.execute(patch);
            } catch (RuntimeException e) {
                log.warn("Schema compatibility patch skipped ({}): {}", patch, e.getMessage());
            }
        }
    }
}
