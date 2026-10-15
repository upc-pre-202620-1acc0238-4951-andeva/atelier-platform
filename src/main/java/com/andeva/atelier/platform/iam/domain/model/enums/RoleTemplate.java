package com.andeva.atelier.platform.iam.domain.model.enums;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.PermissionCatalog;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * The eight factory role templates provisioned to every automotive workshop during tenant onboarding
 * (see {@code docs/backend-documentation/atelier-roles.md}, section 2).
 *
 * @author Joel Huamani Estefanero
 */
public enum RoleTemplate {
    ROLE_WORKSHOP_OWNER(
            "ROLE_WORKSHOP_OWNER",
            "Dueño de Taller",
            "Titular legal y comercial de la cuenta SaaS en Atelier Platform."
    ),
    ROLE_WORKSHOP_ADMINISTRATOR(
            "ROLE_WORKSHOP_ADMINISTRATOR",
            "Administrador de Taller",
            "Gerencia operativa diaria del taller mecánico."
    ),
    ROLE_CHIEF_MECHANIC(
            "ROLE_CHIEF_MECHANIC",
            "Mecánico Jefe",
            "Liderazgo técnico, distribución de carga laboral y aseguramiento de la calidad en patio."
    ),
    ROLE_SERVICE_ADVISOR(
            "ROLE_SERVICE_ADVISOR",
            "Asesor de Servicio",
            "Enlace técnico-comercial directo entre el cliente y el equipo operativo de taller."
    ),
    ROLE_RECEPTIONIST(
            "ROLE_RECEPTIONIST",
            "Recepcionista",
            "Atención de counter de entrada, bienvenida al cliente y gestión de agenda de visitas."
    ),
    ROLE_MECHANIC(
            "ROLE_MECHANIC",
            "Técnico Mecánico",
            "Ejecución directa de labores mecánicas, eléctricas y de diagnóstico en bahía o foso."
    ),
    ROLE_INVENTORY_MANAGER(
            "ROLE_INVENTORY_MANAGER",
            "Encargado de Inventario",
            "Custodia física, catalogación y valoración contable de repuestos, lubricantes e insumos."
    ),
    ROLE_CASHIER(
            "ROLE_CASHIER",
            "Cajero",
            "Recaudación económica, cobranza y liquidación tributaria de servicios."
    );

    private final String code;
    private final String defaultName;
    private final String description;

    RoleTemplate(String code, String defaultName, String description) {
        this.code = code;
        this.defaultName = defaultName;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String defaultName() {
        return defaultName;
    }

    public String description() {
        return description;
    }

    /**
     * Resolves the factory template of a persisted system role from its canonical code.
     */
    public static Optional<RoleTemplate> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(template -> template.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    /**
     * Names of the catalog permissions granted to this template by default.
     */
    public Set<String> defaultPermissionNames() {
        return PermissionCatalog.defaultsFor(this);
    }
}
