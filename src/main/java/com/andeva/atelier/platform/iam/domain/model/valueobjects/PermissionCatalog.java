package com.andeva.atelier.platform.iam.domain.model.valueobjects;

import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate.ROLE_INVENTORY_MANAGER;
import static com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate.ROLE_MECHANIC;
import static com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate.ROLE_SERVICE_ADVISOR;
import static com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate.ROLE_WORKSHOP_ADMIN;
import static com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate.ROLE_WORKSHOP_OWNER;

/**
 * Immutable platform catalog of atomic RBAC permissions and the factory default grants of each {@link RoleTemplate}.
 * <p>
 * Every permission mirrors an authority required by a {@code @PreAuthorize} expression of the REST API, so the
 * catalog is defined by code (never by workshops). It is seeded idempotently on startup and each template role
 * receives exactly the subset declared here (see {@code docs/backend-documentation/atelier-roles.md}).
 *
 * @author Joel Huamani Estefanero
 */
public final class PermissionCatalog {

    /**
     * Atomic permission definition of the platform catalog.
     */
    public record Entry(String name, String description, Set<RoleTemplate> grantedTo) {

        public Entry {
            Objects.requireNonNull(name, "Permission name cannot be null");
            Objects.requireNonNull(description, "Permission description cannot be null");
            grantedTo = Collections.unmodifiableSet(grantedTo.isEmpty()
                    ? EnumSet.noneOf(RoleTemplate.class)
                    : EnumSet.copyOf(grantedTo));
        }

        /**
         * Bounded context prefix of the permission in upper case (e.g. {@code IAM}, {@code OPERATIONS}).
         */
        public String category() {
            return name.substring(0, name.indexOf(':')).toUpperCase(Locale.ROOT);
        }
    }

    private static final Set<RoleTemplate> O = Set.of(ROLE_WORKSHOP_OWNER);
    private static final Set<RoleTemplate> OA = Set.of(ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN);
    private static final Set<RoleTemplate> OAS = Set.of(ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN, ROLE_SERVICE_ADVISOR);
    private static final Set<RoleTemplate> OAI = Set.of(ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN, ROLE_INVENTORY_MANAGER);
    private static final Set<RoleTemplate> OAM = Set.of(ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN, ROLE_MECHANIC);
    private static final Set<RoleTemplate> OM = Set.of(ROLE_WORKSHOP_OWNER, ROLE_MECHANIC);
    private static final Set<RoleTemplate> OASM = Set.of(
            ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN, ROLE_SERVICE_ADVISOR, ROLE_MECHANIC);
    private static final Set<RoleTemplate> OAMI = Set.of(
            ROLE_WORKSHOP_OWNER, ROLE_WORKSHOP_ADMIN, ROLE_MECHANIC, ROLE_INVENTORY_MANAGER);
    private static final Set<RoleTemplate> M = Set.of(ROLE_MECHANIC);
    private static final Set<RoleTemplate> SMI = Set.of(ROLE_SERVICE_ADVISOR, ROLE_MECHANIC, ROLE_INVENTORY_MANAGER);
    private static final Set<RoleTemplate> ALL = Set.of(RoleTemplate.values());

    private static final List<Entry> ENTRIES = List.of(
            // IAM & Tenancy
            new Entry("iam:tenants:read", "View the current workshop profile and configuration", ALL),
            new Entry("iam:tenants:update", "Update the workshop name and legal name", O),
            new Entry("iam:branches:read", "View branches and their geofences", ALL),
            new Entry("iam:branches:manage", "Create branches and configure GPS location and geofence radius", OA),
            new Entry("iam:members:read", "View workshop staff and their membership status", OA),
            new Entry("iam:members:invite", "Send job invitations to new staff members", OA),
            new Entry("iam:members:manage_roles", "Assign or revoke security roles of staff members", OA),
            new Entry("iam:members:compensate", "Change staff compensation schemes", O),
            new Entry("iam:roles:read", "View factory and custom roles of the workshop", OA),
            new Entry("iam:roles:create", "Create custom roles and edit role permissions", OA),
            new Entry("iam:permissions:read", "View the platform permission catalog", OA),

            // CRM & Fleet
            new Entry("crm:customers:read", "Search and view customers", OAS),
            new Entry("crm:customers:create", "Register new vehicle owner customers", OAS),
            new Entry("crm:customers:update", "Update customer personal or tax data", OAS),
            new Entry("crm:customers:manage", "Manage customer account memberships", OAS),
            new Entry("crm:vehicles:read", "Search and view vehicles and their service history", ALL),
            new Entry("crm:vehicles:create", "Register new vehicles and link them to their owner", OAS),
            new Entry("crm:vehicles:update", "Update mileage and technical specifications of vehicles", OAS),
            new Entry("crm:fleets:manage", "Manage corporate fleets and B2B contracts", OAS),
            new Entry("crm:appointments:read", "View workshop appointments", OAS),
            new Entry("crm:appointments:create", "Book customer appointments", OAS),
            new Entry("crm:appointments:update", "Confirm, check in, reschedule or cancel appointments", OAS),
            new Entry("crm:appointments:manage", "Fully manage the appointment lifecycle", OAS),

            // Workshop Operations (MRO)
            new Entry("operations:work-orders:read", "View work orders, statuses and details", ALL),
            new Entry("operations:work-orders:write", "Open, update, assign, deliver and cancel work orders", OAS),
            new Entry("operations:services:read", "View the workshop service catalog", OASM),
            new Entry("operations:services:write", "Create and update workshop services", OA),
            new Entry("operations:tasks:read", "View mechanical tasks", ALL),
            new Entry("operations:tasks:update", "Update tasks, reassign mechanics and manage task parts", OAS),
            new Entry("operations:tasks:write", "Fully manage mechanical tasks", OA),
            new Entry("operations:tasks:track_time", "Start, hold and resume labor timers", M),
            new Entry("operations:tasks:upload_photos", "Attach disassembly and assembly evidence photos", M),
            new Entry("operations:tasks:complete", "Register the technical completion of a task", M),
            new Entry("operations:bays:read", "View work bay availability and occupancy", OASM),
            new Entry("operations:bays:write", "Create work bays and change their maintenance status", OA),
            new Entry("operations:bays:manage", "Fully manage work bays", OA),

            // Inventory & Supply Chain
            new Entry("inventory:parts:read", "View the spare parts catalog, prices and stock levels", ALL),
            new Entry("inventory:parts:manage", "Create and update spare part specifications", OAI),
            new Entry("inventory:suppliers:read", "View commercial suppliers", OAI),
            new Entry("inventory:suppliers:manage", "Manage commercial suppliers", OAI),
            new Entry("inventory:purchase_orders:read", "View purchase orders issued to suppliers", OAI),
            new Entry("inventory:purchase_orders:create", "Create purchase orders", OAI),
            new Entry("inventory:purchase_orders:receive", "Receive purchase orders into stock", OAI),
            new Entry("inventory:purchase_orders:cancel", "Cancel purchase orders", OAI),
            new Entry("inventory:purchase_orders:manage", "Fully manage purchase orders", OAI),
            new Entry("inventory:batches:read", "View stock batches and their unit costs", OAI),
            new Entry("inventory:batches:receive", "Receive stock batches with unit acquisition cost", OAI),
            new Entry("inventory:batches:dispatch_fifo", "Dispatch parts to tasks under FIFO costing", OAMI),
            new Entry("inventory:batches:restore", "Restore parts removed from cancelled tasks", OAI),
            new Entry("inventory:batches:manage", "Fully manage stock batches", OAI),
            new Entry("inventory:alerts:read", "View minimum stock alerts", OAI),
            new Entry("inventory:alerts:evaluate", "Evaluate stock levels to raise replenishment alerts", OAI),
            new Entry("inventory:alerts:resolve", "Resolve replenishment alerts", OAI),
            new Entry("inventory:alerts:manage", "Fully manage stock alerts", OAI),

            // Human Resources & Shifts
            new Entry("hr:attendance:clock_in", "Clock in with GPS geofence validation", SMI),
            new Entry("hr:attendance:clock_out", "Clock out and settle the effective workday", SMI),
            new Entry("hr:attendance:read_own", "View own attendance history", ALL),
            new Entry("hr:attendance:audit_all", "Audit attendance and perimeter anomalies of the workshop", OA),
            new Entry("hr:shifts:read", "View weekly shift schedules and tolerances", ALL),
            new Entry("hr:shifts:manage", "Configure shifts, working hours and grace margins", OA),
            new Entry("hr:justifications:approve", "Approve or reject lateness justifications", OA),
            new Entry("hr:plame:export", "Export SUNAT PLAME compatible workday reports", OA),

            // Invoicing & Compliance
            new Entry("invoicing:invoices:read", "View issued vouchers, credit notes and CDR statuses", OAS),
            new Entry("invoicing:invoices:issue_sunat", "Issue UBL 2.1 electronic vouchers to SUNAT", OA),
            new Entry("invoicing:payments:create", "Register multi-method payments", OA),
            new Entry("invoicing:cashflow:export_pdf", "Export the cash flow report as PDF", O),
            new Entry("invoicing:fiscal_config:manage", "Configure digital certificates, PSE link and SUNAT series", O),

            // SaaS Billing & Subscriptions
            new Entry("billing:subscriptions:read", "View the contracted SaaS plan and usage quotas", OA),
            new Entry("billing:subscriptions:manage_stripe", "Manage the SaaS subscription through Stripe", O),
            new Entry("billing:invoices:read", "View invoices for the Atelier SaaS service", O),

            // IoT Telemetry & Predictive Maintenance
            new Entry("iot:devices:read", "View OBD-II scanners and hardware status", OASM),
            new Entry("iot:devices:register", "Register new OBD-II scanners", OA),
            new Entry("iot:devices:manage", "Update and decommission OBD-II scanners", OA),
            new Entry("iot:installations:read", "View scanner installations on vehicles", OASM),
            new Entry("iot:installations:manage", "Install or uninstall scanners on vehicles", OASM),
            new Entry("iot:telemetry:ingest", "Ingest telemetry batches", OM),
            new Entry("iot:telemetry:read", "View live and aggregated telemetry", OASM),
            new Entry("iot:faults:read", "View active electronic faults and DTC codes", OASM),
            new Entry("iot:faults:write", "Register fault codes read from the vehicle", OAM),
            new Entry("iot:faults:resolve", "Verify the clearing of engine fault codes", OAM),
            new Entry("iot:alerts:read", "View predictive anomaly alerts", OASM),
            new Entry("iot:alerts:acknowledge", "Acknowledge predictive alerts", OASM),
            new Entry("iot:alerts:convert", "Convert predictive alerts into work orders", OAS),
            new Entry("iot:health_reports:generate", "Request AI vehicle health reports", OASM),
            new Entry("iot:health_reports:read", "Download vehicle health reports", OASM)
    );

    private static final Map<String, Entry> BY_NAME;
    private static final Map<RoleTemplate, Set<String>> DEFAULTS_BY_TEMPLATE;

    static {
        Map<String, Entry> byName = new LinkedHashMap<>();
        Map<RoleTemplate, Set<String>> defaults = new EnumMap<>(RoleTemplate.class);
        for (RoleTemplate template : RoleTemplate.values()) {
            defaults.put(template, new LinkedHashSet<>());
        }
        for (Entry entry : ENTRIES) {
            if (byName.put(entry.name(), entry) != null) {
                throw new IllegalStateException("Duplicate permission in catalog: " + entry.name());
            }
            entry.grantedTo().forEach(template -> defaults.get(template).add(entry.name()));
        }
        defaults.replaceAll((template, names) -> Collections.unmodifiableSet(names));
        BY_NAME = Collections.unmodifiableMap(byName);
        DEFAULTS_BY_TEMPLATE = Collections.unmodifiableMap(defaults);
    }

    private PermissionCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static Set<String> names() {
        return BY_NAME.keySet();
    }

    public static boolean contains(String permissionName) {
        return BY_NAME.containsKey(permissionName);
    }

    /**
     * Permission names granted by default to the given factory template.
     */
    public static Set<String> defaultsFor(RoleTemplate template) {
        Objects.requireNonNull(template, "Role template cannot be null");
        return DEFAULTS_BY_TEMPLATE.get(template);
    }
}
