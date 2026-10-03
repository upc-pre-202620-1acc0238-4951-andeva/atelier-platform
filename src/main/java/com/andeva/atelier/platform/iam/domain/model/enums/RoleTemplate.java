package com.andeva.atelier.platform.iam.domain.model.enums;

/**
 * Standard factory role templates provisioned to automotive workshops during tenant onboarding.
 *
 * @author Joel Huamani Estefanero
 */
public enum RoleTemplate {
    ROLE_WORKSHOP_OWNER(
            "ROLE_WORKSHOP_OWNER",
            "Workshop Owner",
            "Sovereign owner with unconstrained administrative and operational privileges across the entire workshop tenant."
    ),
    ROLE_WORKSHOP_ADMIN(
            "ROLE_WORKSHOP_ADMIN",
            "Workshop Administrator",
            "Executive manager responsible for operations, staff management, and branch supervision."
    ),
    ROLE_SERVICE_ADVISOR(
            "ROLE_SERVICE_ADVISOR",
            "Service Advisor",
            "Front-desk specialist managing customer check-ins, diagnostic quotes, and service appointments."
    ),
    ROLE_MECHANIC(
            "ROLE_MECHANIC",
            "Mechanic",
            "Technical specialist executing multi-point inspections, maintenance work orders, and labor logs."
    ),
    ROLE_INVENTORY_MANAGER(
            "ROLE_INVENTORY_MANAGER",
            "Inventory Manager",
            "Warehouse supervisor controlling spare parts catalog, stock replenishments, and physical counts."
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
}
