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

/**
 * Immutable platform catalog of atomic RBAC permissions and the factory default grants of each {@link RoleTemplate}.
 * <p>
 * Names, descriptions and the role matrix mirror sections 4 and 5 of
 * {@code docs/backend-documentation/atelier-roles.md}, which is the source of truth (a test keeps both in sync).
 * Endpoints authorize exclusively with these permissions; roles only group them. The catalog is seeded
 * idempotently on startup and each factory role receives exactly the subset declared here.
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

    private static final RoleTemplate OWNER = RoleTemplate.ROLE_WORKSHOP_OWNER;
    private static final RoleTemplate ADMINISTRATOR = RoleTemplate.ROLE_WORKSHOP_ADMINISTRATOR;
    private static final RoleTemplate CHIEF_MECHANIC = RoleTemplate.ROLE_CHIEF_MECHANIC;
    private static final RoleTemplate SERVICE_ADVISOR = RoleTemplate.ROLE_SERVICE_ADVISOR;
    private static final RoleTemplate RECEPTIONIST = RoleTemplate.ROLE_RECEPTIONIST;
    private static final RoleTemplate MECHANIC = RoleTemplate.ROLE_MECHANIC;
    private static final RoleTemplate INVENTORY_MANAGER = RoleTemplate.ROLE_INVENTORY_MANAGER;
    private static final RoleTemplate CASHIER = RoleTemplate.ROLE_CASHIER;

    private static final List<Entry> ENTRIES = List.of(
            // IAM & Tenancy
            new Entry("iam:tenants:read", "Consultar perfil y configuración del taller actual", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("iam:tenants:update", "Actualizar nombre y razón social del taller", of(OWNER)),
            new Entry("iam:branches:read", "Consultar sedes de operación y geocercas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("iam:branches:manage", "Crear sedes y configurar coordenadas GPS y radio de geocerca", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:members:read", "Consultar personal del taller y sus estados", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("iam:members:invite", "Emitir invitaciones laborales a nuevos colaboradores por correo", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:members:manage_roles", "Asignar o revocar roles de seguridad a miembros del taller", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:members:compensate", "Modificar esquemas de remuneración salarial (fijo o por hora)", of(OWNER)),
            new Entry("iam:roles:read", "Consultar roles de fábrica y personalizados del taller", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:roles:create", "Crear roles personalizados combinando permisos del catálogo", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:permissions:read", "Consultar el catálogo transversal de permisos atómicos", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:members:deactivate", "Dar de baja la membresía de un colaborador del taller", of(OWNER, ADMINISTRATOR)),
            new Entry("iam:roles:manage", "Editar permisos, restablecer a fábrica y eliminar roles del taller", of(OWNER, ADMINISTRATOR)),

            // CRM & Fleet
            new Entry("crm:customers:read", "Buscar y consultar clientes por documento o datos de contacto", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, CASHIER)),
            new Entry("crm:customers:create", "Dar de alta a nuevos clientes propietarios de vehículos", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:customers:update", "Modificar datos personales o fiscales del cliente", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:vehicles:read", "Consultar vehículos por placa, VIN o historial de servicio", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("crm:vehicles:create", "Registrar nuevos vehículos y asociarlos a su propietario", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:vehicles:update", "Actualizar odómetro, kilometraje y especificaciones técnicas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:fleets:manage", "Administrar flotas vehiculares corporativas y contratos B2B", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("crm:appointments:read", "Consultar la agenda de citas del taller", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:appointments:create", "Agendar citas de clientes (incluye convertir alertas predictivas en citas)", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("crm:appointments:update", "Confirmar, registrar llegada, reprogramar o cancelar citas", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, RECEPTIONIST)),

            // Workshop Operations (MRO)
            new Entry("operations:work_orders:read", "Consultar órdenes de trabajo, estados y detalles", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("operations:work_orders:create", "Abrir nuevas órdenes de trabajo y configurar tareas iniciales", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR)),
            new Entry("operations:work_orders:cancel", "Cancelar órdenes de trabajo por desistimiento o inviabilidad", of(OWNER, ADMINISTRATOR)),
            new Entry("operations:inspections:create", "Registrar el checklist pericial de recepción e inventario 360°", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR)),
            new Entry("operations:inspections:upload_photos", "Cargar fotografías de evidencias de daños con URLs firmadas S3", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR)),
            new Entry("operations:quotations:read", "Consultar cotizaciones y proformas de mantenimiento", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, INVENTORY_MANAGER, CASHIER)),
            new Entry("operations:quotations:create", "Elaborar presupuestos con cómputo en cascada de mano de obra", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR)),
            new Entry("operations:quotations:send", "Generar y enviar proformas comerciales en PDF al cliente", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("operations:quotations:approve", "Registrar la resolución formal de aprobación del presupuesto", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("operations:proposals:create", "Proponer tareas adicionales por averías detectadas en foso", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("operations:proposals:notify", "Remitir propuesta adicional al cliente para autorización técnica", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("operations:tasks:read", "Consultar tareas mecánicas asignadas a bahías", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC, INVENTORY_MANAGER)),
            new Entry("operations:tasks:track_time", "Iniciar, pausar y reanudar cronómetros de labor efectiva", of(CHIEF_MECHANIC, MECHANIC)),
            new Entry("operations:tasks:upload_photos", "Adjuntar fotografías periciales de desmontaje y montaje", of(CHIEF_MECHANIC, MECHANIC)),
            new Entry("operations:tasks:hold_request", "Solicitar suspensión de tarea por repuesto defectuoso o faltante", of(MECHANIC)),
            new Entry("operations:tasks:hold_validate", "Aprobar o resolver administrativamente la suspensión de tarea", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("operations:tasks:complete", "Registrar el cierre técnico de la labor con cómputo de horas", of(CHIEF_MECHANIC, MECHANIC)),
            new Entry("operations:tasks:charge_parts", "Imputar repuestos del almacén a una tarea propia en curso", of(CHIEF_MECHANIC, MECHANIC)),
            new Entry("operations:bays:read", "Consultar disponibilidad y ocupación de bahías en tiempo real", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("operations:bays:reassign", "Reasignar mecánicos y vehículos entre bahías operativas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("operations:vehicle_handover:execute", "Ejecutar entrega pericial del automóvil y validar pase de salida", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("operations:work_orders:update", "Actualizar órdenes y sus tareas, iniciar y concluir la orden, gestionar repuestos de tareas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR)),
            new Entry("operations:proposals:resolve", "Registrar la aprobación o rechazo del cliente sobre una propuesta adicional", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR)),
            new Entry("operations:services:read", "Consultar el catálogo de servicios del taller", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST)),
            new Entry("operations:services:manage", "Crear y actualizar servicios del catálogo del taller", of(OWNER, ADMINISTRATOR)),
            new Entry("operations:bays:manage", "Crear bahías y cambiar su estado de mantenimiento", of(OWNER, ADMINISTRATOR)),

            // Inventory & Supply Chain
            new Entry("inventory:parts:read", "Consultar catálogo de repuestos, precios y niveles de stock", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC, INVENTORY_MANAGER)),
            new Entry("inventory:parts:manage", "Crear y actualizar especificaciones de piezas y compatibilidades", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:suppliers:manage", "Administrar catálogo de proveedores comerciales", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:purchase_orders:read", "Consultar órdenes de compra emitidas a proveedores", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, INVENTORY_MANAGER)),
            new Entry("inventory:purchase_orders:create", "Crear y formalizar órdenes de compra de abastecimiento", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:batches:receive", "Registrar ingreso físico de lotes de repuestos con costeo unitario", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:batches:dispatch_fifo", "Ejecutar descargo contable automatizado bajo método FIFO", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:batches:restore", "Reincorporar repuestos removidos de tareas canceladas", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:alerts:read", "Consultar alertas de reposición por stock mínimo", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, INVENTORY_MANAGER)),
            new Entry("inventory:alerts:resolve", "Marcar alertas resueltas tras emitir orden de reposición", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),
            new Entry("inventory:batches:read", "Consultar lotes de repuestos, costos unitarios y valorización del inventario", of(OWNER, ADMINISTRATOR, INVENTORY_MANAGER)),

            // Human Resources & Shifts
            new Entry("hr:attendance:clock_in", "Registrar marcación de ingreso laboral con validación de geocerca GPS", of(CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("hr:attendance:clock_out", "Registrar marcación de salida y liquidar jornada efectiva", of(CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("hr:attendance:read_own", "Consultar historial propio de asistencias y tiempos de labor", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("hr:attendance:audit_all", "Auditar marcaciones, anomalías perimétricas y asistencias del taller", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("hr:shifts:read", "Consultar programación semanal de turnos y tolerancias", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("hr:shifts:manage", "Configurar turnos, horarios laborales y márgenes de gracia", of(OWNER, ADMINISTRATOR)),
            new Entry("hr:justifications:create", "Enviar descargo justificatorio de tardanza o ausencia", of(CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("hr:justifications:approve", "Aprobar o rechazar descargos de tardanza de colaboradores", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("hr:plame:export", "Exportar reportes de jornada laboral compatibles con SUNAT PLAME", of(OWNER, ADMINISTRATOR)),
            new Entry("hr:employees:manage", "Registrar perfiles laborales y cambiar el estado de empleo del personal", of(OWNER, ADMINISTRATOR)),
            new Entry("hr:payroll:manage", "Generar, ajustar, aprobar y desembolsar planillas de pago", of(OWNER, ADMINISTRATOR)),

            // Invoicing & Compliance
            new Entry("invoicing:invoices:read", "Consultar comprobantes emitidos, notas de crédito y estados CDR", of(OWNER, ADMINISTRATOR, SERVICE_ADVISOR, CASHIER)),
            new Entry("invoicing:invoices:settle", "Preliquidar económicamente órdenes de trabajo finalizadas", of(OWNER, ADMINISTRATOR, CASHIER)),
            new Entry("invoicing:invoices:issue_sunat", "Emitir comprobantes tributarios electrónicos UBL 2.1 ante SUNAT vía PSE", of(OWNER, ADMINISTRATOR, CASHIER)),
            new Entry("invoicing:payments:create", "Registrar cobros y amortizaciones multimoneda y multimedio", of(OWNER, ADMINISTRATOR, CASHIER)),
            new Entry("invoicing:exit_passes:issue", "Emitir pase digital de salida del vehículo tras orden pagada", of(OWNER, ADMINISTRATOR, CASHIER)),
            new Entry("invoicing:cashflow:export_pdf", "Generar y exportar reporte contable de flujo de caja en PDF", of(OWNER)),
            new Entry("invoicing:fiscal_config:manage", "Configurar certificados digitales, enlace PSE y series SUNAT", of(OWNER)),

            // SaaS Billing & Subscriptions
            new Entry("billing:subscriptions:read", "Consultar plan SaaS contratado, cuotas de uso y fecha de corte", of(OWNER, ADMINISTRATOR)),
            new Entry("billing:subscriptions:manage_stripe", "Gestionar suscripción B2B, cambio de plan y portal de facturación Stripe", of(OWNER)),
            new Entry("billing:plans:read", "Consultar catálogo público de planes comerciales y tarifas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, RECEPTIONIST, MECHANIC, INVENTORY_MANAGER, CASHIER)),
            new Entry("billing:invoices:read", "Consultar facturas por el uso del servicio SaaS de Atelier", of(OWNER)),

            // IoT Telemetry & Predictive Maintenance
            new Entry("iot:devices:read", "Consultar catálogo de escáneres OBD-II y estados de hardware", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:devices:register", "Registrar nuevos escáneres OBD-II por dirección MAC y serial", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC)),
            new Entry("iot:installations:manage", "Vincular o desvincular escáneres a vehículos en bahía", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:telemetry:ingest", "Ingestar lotes de métricas telemétricas hacia TimescaleDB", of(MECHANIC)),
            new Entry("iot:telemetry:read", "Consultar lecturas telemétricas en vivo para tacómetros y análisis", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:faults:read", "Consultar fallas electrónicas activas y códigos DTC leídos", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:faults:resolve", "Verificar borrado y subsanación de códigos de falla en motor", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, MECHANIC)),
            new Entry("iot:alerts:read", "Consultar alertas analíticas generadas por anomalías térmicas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:alerts:acknowledge", "Confirmar recepción y lectura de alertas predictivas", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, MECHANIC)),
            new Entry("iot:health_reports:generate", "Solicitar inferencia pericial con Spring AI Groq Cloud LPU", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC)),
            new Entry("iot:health_reports:read", "Descargar reporte forense de salud automotriz en PDF", of(OWNER, ADMINISTRATOR, CHIEF_MECHANIC, SERVICE_ADVISOR, MECHANIC))
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

    private static Set<RoleTemplate> of(RoleTemplate... templates) {
        return Set.of(templates);
    }

    private static Set<RoleTemplate> none() {
        return Set.of();
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
