package com.andeva.atelier.platform.demo;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.hr.application.commandservices.EmployeeProfileCommandService;
import com.andeva.atelier.platform.hr.application.commandservices.WorkShiftCommandService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.CreateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RegisterEmployeeProfileCommand;
import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.commandservices.InvitationCommandService;
import com.andeva.atelier.platform.iam.application.commandservices.TenantCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.RoleQueryService;
import com.andeva.atelier.platform.iam.application.queryservices.TenantQueryService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Tenant;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AcceptInvitationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.CreateTenantCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.InviteStaffCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.domain.model.enums.RoleTemplate;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetRolesByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetTenantByTaxIdQuery;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.inventory.application.commandservices.InventoryItemCommandService;
import com.andeva.atelier.platform.inventory.application.commandservices.SupplierCommandService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddInventoryBatchCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreateInventoryItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.iot.application.commandservices.DeviceInstallationCommandService;
import com.andeva.atelier.platform.iot.application.commandservices.Obd2DeviceCommandService;
import com.andeva.atelier.platform.iot.application.commandservices.TelemetryIngestionCommandService;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleFaultCommandService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.ConnectionType;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkBayCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.AddTaskToWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.AssignWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.BayType;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Creates, once, a complete demo workshop for presentations: owner on an active Max plan, a mechanic with shift and
 * profile, two customers (person and company) with a vehicle each, OBD-II devices with several days of telemetry,
 * an inventory with stock, a service catalog, a bay and a work order assigned to the mechanic.
 * <p>
 * Disabled unless {@code DEMO_SEED_ENABLED=true}. Emails and passwords come from environment variables, never from
 * code. When DEMO_ADMIN_EMAIL already registered a workshop, that workshop is filled instead of creating one. It does
 * nothing once the demo vehicles exist, so it can stay enabled across restarts.
 *
 * @author Joel Huamani Estefanero
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoWorkshopSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoWorkshopSeeder.class);
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final int TELEMETRY_DAYS = 10;
    private static final String SEDAN_PLATE = "BXR482";

    private final DemoSeedProperties props;
    private final TenantCommandService tenantCommandService;
    private final TenantQueryService tenantQueryService;
    private final UserRepository userRepository;
    private final BCryptHashingService hashingService;
    private final BranchQueryService branchQueryService;
    private final BranchCommandService branchCommandService;
    private final RoleQueryService roleQueryService;
    private final InvitationCommandService invitationCommandService;
    private final TenantSubscriptionCommandService subscriptionCommandService;
    private final WorkShiftCommandService workShiftCommandService;
    private final EmployeeProfileCommandService employeeProfileCommandService;
    private final CustomerCommandService customerCommandService;
    private final VehicleCommandService vehicleCommandService;
    private final SupplierCommandService supplierCommandService;
    private final InventoryItemCommandService inventoryItemCommandService;
    private final ServiceCommandService serviceCommandService;
    private final WorkBayCommandService workBayCommandService;
    private final WorkOrderCommandService workOrderCommandService;
    private final Obd2DeviceCommandService deviceCommandService;
    private final DeviceInstallationCommandService installationCommandService;
    private final TelemetryIngestionCommandService telemetryCommandService;
    private final VehicleFaultCommandService faultCommandService;
    private final TenantMembershipRepository membershipRepository;
    private final VehicleRepository vehicleRepository;

    public DemoWorkshopSeeder(DemoSeedProperties props,
                              TenantCommandService tenantCommandService,
                              TenantQueryService tenantQueryService,
                              UserRepository userRepository,
                              BCryptHashingService hashingService,
                              BranchQueryService branchQueryService,
                              BranchCommandService branchCommandService,
                              RoleQueryService roleQueryService,
                              InvitationCommandService invitationCommandService,
                              TenantSubscriptionCommandService subscriptionCommandService,
                              WorkShiftCommandService workShiftCommandService,
                              EmployeeProfileCommandService employeeProfileCommandService,
                              CustomerCommandService customerCommandService,
                              VehicleCommandService vehicleCommandService,
                              SupplierCommandService supplierCommandService,
                              InventoryItemCommandService inventoryItemCommandService,
                              ServiceCommandService serviceCommandService,
                              WorkBayCommandService workBayCommandService,
                              WorkOrderCommandService workOrderCommandService,
                              Obd2DeviceCommandService deviceCommandService,
                              DeviceInstallationCommandService installationCommandService,
                              TelemetryIngestionCommandService telemetryCommandService,
                              VehicleFaultCommandService faultCommandService,
                              TenantMembershipRepository membershipRepository,
                              VehicleRepository vehicleRepository) {
        this.props = props;
        this.tenantCommandService = tenantCommandService;
        this.tenantQueryService = tenantQueryService;
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.branchQueryService = branchQueryService;
        this.branchCommandService = branchCommandService;
        this.roleQueryService = roleQueryService;
        this.invitationCommandService = invitationCommandService;
        this.subscriptionCommandService = subscriptionCommandService;
        this.workShiftCommandService = workShiftCommandService;
        this.employeeProfileCommandService = employeeProfileCommandService;
        this.customerCommandService = customerCommandService;
        this.vehicleCommandService = vehicleCommandService;
        this.supplierCommandService = supplierCommandService;
        this.inventoryItemCommandService = inventoryItemCommandService;
        this.serviceCommandService = serviceCommandService;
        this.workBayCommandService = workBayCommandService;
        this.workOrderCommandService = workOrderCommandService;
        this.deviceCommandService = deviceCommandService;
        this.installationCommandService = installationCommandService;
        this.telemetryCommandService = telemetryCommandService;
        this.faultCommandService = faultCommandService;
        this.membershipRepository = membershipRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Order(10)
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            seed();
        } catch (RuntimeException e) {
            log.error("Demo workshop could not be seeded: {}", e.getMessage(), e);
        }
    }

    /**
     * @return true when the demo was created now, false when it already existed or the configuration is missing
     */
    public boolean seed() {
        if (!props.isComplete()) {
            log.warn("Demo seed enabled but DEMO_ADMIN_EMAIL, DEMO_MECHANIC_EMAIL or DEMO_MECHANIC_PASSWORD is missing");
            return false;
        }
        if (vehicleRepository.existsByPlate(LicensePlate.of(SEDAN_PLATE))) {
            log.info("Demo data (vehicle {}) already exists: nothing to seed", SEDAN_PLATE);
            return false;
        }

        // 1. Workshop and owner: the owner's existing workshop is adopted, otherwise one is registered
        //    through the sign-up flow (roles, main branch and free trial)
        TenantId tenantId;
        Optional<User> existingOwner = userRepository.findByEmail(EmailAddress.of(props.adminEmail()));
        if (existingOwner.isPresent()) {
            Optional<TenantId> owned = workshopRegisteredBy(existingOwner.get());
            if (owned.isEmpty()) {
                log.warn("DEMO_ADMIN_EMAIL {} already has an account but it did not register any workshop: nothing to seed", props.adminEmail());
                return false;
            }
            tenantId = owned.get();
            log.info("Seeding the demo into the existing workshop {} of {}", tenantId.value(), props.adminEmail());
        } else {
            if (props.adminPassword().isBlank()) {
                log.warn("DEMO_ADMIN_PASSWORD is required to register a new demo workshop");
                return false;
            }
            if (tenantQueryService.handle(new GetTenantByTaxIdQuery(TaxId.of(props.workshopRuc()))).isPresent()) {
                log.warn("RUC {} already belongs to another workshop: set DEMO_WORKSHOP_RUC to a free one", props.workshopRuc());
                return false;
            }
            log.info("Seeding the demo workshop '{}'", props.workshopName());
            Tenant tenant = require("workshop", tenantCommandService.handle(new CreateTenantCommand(
                    props.workshopName(),
                    props.workshopLegalName(),
                    TaxId.of(props.workshopRuc()),
                    EmailAddress.of(props.adminEmail()),
                    Password.of(hashingService.hash(props.adminPassword())),
                    PersonName.of(props.adminFirstName(), props.adminLastName()),
                    PhoneNumber.of(props.adminPhone()),
                    null)));
            tenantId = tenant.id();
        }
        User owner = userRepository.findByEmail(EmailAddress.of(props.adminEmail())).orElseThrow();
        if (owner.status() != UserStatus.ACTIVE) {
            owner.verifyEmail();
            userRepository.save(owner);
        }

        // 2. Active Max plan for the whole course, then the branch where the class takes place
        subscriptionCommandService.handleGrantComplimentaryPlan(tenantId, PlanTier.MAX, props.planDays());
        Branch mainBranch = branchQueryService.handle(new GetBranchesByTenantIdQuery(tenantId)).getFirst();
        require("branch", branchCommandService.handle(new UpdateBranchLocationCommand(mainBranch.id(), props.branchName(),
                mainBranch.sunatCode(), GeoPoint.of(props.branchLatitude(), props.branchLongitude()), props.branchRadiusMeters())));
        BranchId branchId = mainBranch.id();

        // 3. Mechanic: invitation accepted with the configured password, shift Monday to Saturday and HR profile
        Role mechanicRole = roleQueryService.handle(new GetRolesByTenantIdQuery(tenantId)).stream()
                .filter(role -> RoleTemplate.ROLE_MECHANIC.code().equals(role.code()))
                .findFirst().orElseThrow();
        Invitation invitation = require("mechanic invitation", invitationCommandService.handle(
                new InviteStaffCommand(tenantId, EmailAddress.of(props.mechanicEmail()), mechanicRole.id(), Duration.ofHours(48))));
        AuthenticatedUser mechanic = require("mechanic account", invitationCommandService.handle(new AcceptInvitationCommand(
                invitation.token(), props.mechanicPassword(), props.mechanicFirstName(), props.mechanicLastName(), props.mechanicPhone())));
        UUID mechanicMembershipId = mechanic.membershipId();

        WorkShift shift = require("shift", workShiftCommandService.handle(new CreateWorkShiftCommand(tenantId, "Turno mañana",
                LocalTime.of(8, 0), LocalTime.of(17, 0), 15, EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY))));
        require("mechanic profile", employeeProfileCommandService.handle(new RegisterEmployeeProfileCommand(tenantId, branchId,
                TenantMembershipId.of(mechanicMembershipId), shift.getId(), Money.soles(new BigDecimal("2200.00")),
                CompensationType.MONTHLY_FIXED, "Técnico mecánico")));

        // 4. Customers and their vehicles
        Customer person = require("individual customer", customerCommandService.handle(new RegisterIndividualCustomerCommand(
                tenantId, "Carlos", "Ramírez Torres", "45678912", props.aliasOfAdmin("carlos"), "+51987654321")));
        Customer company = require("company customer", customerCommandService.handle(new RegisterCompanyCustomerCommand(
                tenantId, "Transportes Andinos S.A.C.", "20601234573", props.aliasOfAdmin("transportes"), "+51912345678")));
        Vehicle sedan = require("sedan", vehicleCommandService.handle(new RegisterVehicleCommand(tenantId, SEDAN_PLATE,
                "JTDBR32E720123456", "Toyota", "Yaris", 2019, EngineType.GASOLINE, Optional.of(person.id()), Optional.empty())));
        Vehicle van = require("van", vehicleCommandService.handle(new RegisterVehicleCommand(tenantId, "D4K719",
                "KMHWH81KBMU123456", "Hyundai", "H-1", 2021, EngineType.DIESEL, Optional.of(company.id()), Optional.empty())));

        // 5. Inventory with stock, service catalog and a bay
        Supplier supplier = require("supplier", supplierCommandService.handle(new RegisterSupplierCommand(tenantId,
                "Repuestos del Sur E.I.R.L.",
                new com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId("20512345671"),
                "Lucía Paredes", "+51944555666", props.aliasOfAdmin("proveedor"), "Av. Industrial 1450, Lima")));
        part(tenantId, supplier, "Aceite sintético 5W-30 (1 L)", "OIL-5W30-1L", ItemCategory.LUBRICANTS, "45.00", "28.00", 40, 10, "LITER");
        part(tenantId, supplier, "Filtro de aceite", "FLT-OIL-YRS", ItemCategory.FILTERS, "38.00", "22.00", 12, 4, "UNIT");
        part(tenantId, supplier, "Refrigerante orgánico (1 gal)", "CLT-ORG-1G", ItemCategory.ENGINE, "65.00", "40.00", 6, 5, "GALLON");
        part(tenantId, supplier, "Pastillas de freno delanteras", "BRK-PAD-H1", ItemCategory.BRAKES, "180.00", "110.00", 8, 2, "UNIT");
        part(tenantId, supplier, "Batería 12 V 75 Ah", "BAT-12V-75", ItemCategory.ELECTRICAL, "420.00", "300.00", 3, 2, "UNIT");

        Service oilChange = require("service", serviceCommandService.handle(new CreateServiceItemCommand(tenantId,
                "Cambio de aceite y filtro", new BigDecimal("120.00"), "PEN", 45)));
        Service cooling = require("service", serviceCommandService.handle(new CreateServiceItemCommand(tenantId,
                "Revisión del sistema de refrigeración", new BigDecimal("180.00"), "PEN", 90)));
        Service obdScan = require("service", serviceCommandService.handle(new CreateServiceItemCommand(tenantId,
                "Diagnóstico electrónico OBD-II", new BigDecimal("90.00"), "PEN", 30)));
        WorkBay lift = require("bay", workBayCommandService.handle(new CreateWorkBayCommand(tenantId, branchId, "Elevador 1", BayType.LIFT)));
        require("bay", workBayCommandService.handle(new CreateWorkBayCommand(tenantId, branchId, "Elevador 2", BayType.LIFT)));

        // 6. OBD-II devices and telemetry: the sedan is healthy, the van overheats with a weak battery in the last days
        installDevice(tenantId, sedan.id(), "AA:01:23:45:67:01", 48210);
        installDevice(tenantId, van.id(), "AA:01:23:45:67:02", 96540);
        ingest(tenantId, sedan.id(), false);
        ingest(tenantId, van.id(), true);
        faultCommandService.handle(new RegisterVehicleFaultCommand(van.id(), tenantId, DtcCode.of("P0217"), FaultSeverity.CRITICAL,
                "Engine coolant over temperature condition"));
        faultCommandService.handle(new RegisterVehicleFaultCommand(van.id(), tenantId, DtcCode.of("P0562"), FaultSeverity.MEDIUM,
                "System voltage low"));

        // 7. A work order on the van with two tasks for the mechanic, in the first bay
        WorkOrder order = require("work order", workOrderCommandService.handle(new CreateWorkOrderCommand(tenantId, branchId, null,
                van.id(), company.id(), Mileage.of(96540),
                DiagnosticSummary.of("La unidad se sobrecalienta en subida y el testigo de batería se enciende al arrancar"))));
        require("task", workOrderCommandService.handle(new AddTaskToWorkOrderCommand(order.getId(), obdScan.getId(),
                mechanicMembershipId, "Lectura de códigos y prueba de carga de la batería", new BigDecimal("90.00"), new BigDecimal("0.5"), "PEN")));
        require("task", workOrderCommandService.handle(new AddTaskToWorkOrderCommand(order.getId(), cooling.getId(),
                mechanicMembershipId, "Revisar termostato, bomba de agua y nivel de refrigerante", new BigDecimal("180.00"), new BigDecimal("1.5"), "PEN")));
        require("bay", workOrderCommandService.handle(new AssignWorkBayCommand(order.getId(), lift.getId())));

        log.info("Demo workshop seeded: tenant {}, owner {}, mechanic {}, vehicles {} and {}, work order #{}; service '{}' also available",
                tenantId.value(), props.adminEmail(), props.mechanicEmail(), sedan.id().value(), van.id().value(),
                order.getInternalNumber().sequence(), oilChange.getName());
        return true;
    }

    /**
     * The workshop whose first membership (the one created at sign-up) belongs to this account.
     */
    private Optional<TenantId> workshopRegisteredBy(User user) {
        return membershipRepository.findByUserId(user.id()).stream()
                .map(TenantMembership::tenantId)
                .filter(tenantId -> membershipRepository.findEarliestByTenantId(tenantId)
                        .map(first -> first.userId().equals(user.id()))
                        .orElse(false))
                .findFirst();
    }

    private void part(TenantId tenantId, Supplier supplier, String name, String sku, ItemCategory category, String price,
                      String unitCost, int stock, int minimum, String unit) {
        InventoryItem item = require("part " + sku, inventoryItemCommandService.handle(new CreateInventoryItemCommand(tenantId, name,
                Sku.of(sku), category, Money.of(new BigDecimal(price), Currency.PEN), Quantity.of(minimum), unit, null)));
        require("batch " + sku, inventoryItemCommandService.handle(new AddInventoryBatchCommand(item.getId(), supplier.getId(), null,
                "L-" + LocalDate.now(LIMA).getYear() + "-" + sku, Quantity.of(stock), Money.of(new BigDecimal(unitCost), Currency.PEN),
                Instant.now().minus(20, ChronoUnit.DAYS), null)));
    }

    private void installDevice(TenantId tenantId, VehicleId vehicleId, String mac, int odometerKm) {
        DeviceId deviceId = deviceCommandService.handle(new RegisterObd2DeviceCommand(tenantId, DeviceIdentifier.of(mac),
                ConnectionType.BLUETOOTH_BLE, "ELM327 Bluetooth v1.5", "1.5"));
        installationCommandService.handle(new InstallDeviceOnVehicleCommand(deviceId, vehicleId, tenantId, odometerKm));
    }

    /**
     * Ten days of drives (every 20 minutes, 8:00–12:00 Lima). With {@code degrading}, the last three days run hot and
     * the battery sags, which the anomaly engine turns into predictive alerts.
     */
    private void ingest(TenantId tenantId, VehicleId vehicleId, boolean degrading) {
        Random random = new Random(vehicleId.value().getMostSignificantBits());
        List<TelemetryRecord> readings = new ArrayList<>();
        LocalDate firstDay = LocalDate.now(LIMA).minusDays(TELEMETRY_DAYS);
        double fuel = 85;
        for (int day = 0; day < TELEMETRY_DAYS; day++) {
            boolean failing = degrading && day >= TELEMETRY_DAYS - 3;
            for (int slot = 0; slot < 12; slot++) {
                Instant at = firstDay.plusDays(day).atTime(8, 0).plusMinutes(20L * slot).atZone(LIMA).toInstant();
                double temperature = failing ? 106 + day - (TELEMETRY_DAYS - 3) * 1.0 + slot * 0.6 + random.nextDouble() * 2
                        : 86 + random.nextDouble() * 6;
                double voltage = failing ? 12.1 - random.nextDouble() * 0.4 : 13.8 + random.nextDouble() * 0.4;
                int rpm = 800 + random.nextInt(2200);
                int speed = rpm < 1000 ? 0 : 20 + random.nextInt(50);
                fuel = Math.max(15, fuel - 0.4);
                readings.add(TelemetryRecord.of(at, vehicleId, tenantId,
                        GeoCoordinates.of(props.branchLatitude() + random.nextDouble() * 0.02, props.branchLongitude() + random.nextDouble() * 0.02),
                        VehicleSpeed.of(speed), EngineTemperature.of(Math.round(temperature * 10) / 10.0), EngineRpm.of(rpm),
                        FuelLevel.of(Math.round(fuel * 10) / 10.0), BatteryVoltage.of(Math.round(voltage * 100) / 100.0)));
            }
        }
        for (int from = 0; from < readings.size(); from += 200) {
            telemetryCommandService.handle(new IngestTelemetryBatchCommand(vehicleId, tenantId,
                    readings.subList(from, Math.min(from + 200, readings.size()))));
        }
    }

    private static <T> T require(String what, Result<T, ApplicationError> result) {
        if (result.isFailure()) {
            throw new IllegalStateException("Demo seed failed creating the " + what + ": " + result.getError().message());
        }
        return result.getOrThrow();
    }
}
