package com.andeva.atelier.platform.demo;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.hr.application.commandservices.PayrollPaymentCommandService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.ApprovePayrollCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.GeneratePayrollCommand;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.hr.domain.services.HaversineGeofencingService;
import com.andeva.atelier.platform.iam.application.commandservices.BranchCommandService;
import com.andeva.atelier.platform.iam.application.queryservices.BranchQueryService;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateBranchLocationCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.iam.domain.model.queries.GetBranchesByTenantIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.inventory.domain.repositories.InventoryItemRepository;
import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.commandservices.SeriesConfigurationCommandService;
import com.andeva.atelier.platform.invoicing.application.commandservices.VoucherPaymentCommandService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.ConfigureSeriesCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.RegisterVoucherPaymentCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoucherLineCommandDto;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.domain.repositories.SeriesConfigurationRepository;
import com.andeva.atelier.platform.iot.application.commandservices.PredictiveAlertCommandService;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.GeneratePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.domain.repositories.PredictiveAlertRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleHealthReportRepository;
import com.andeva.atelier.platform.operations.application.commandservices.ServiceCommandService;
import com.andeva.atelier.platform.operations.application.commandservices.WorkOrderCommandService;
import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.commands.AddProductToTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.AddTaskToWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CompleteWorkOrderTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.DeliverVehicleCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.MarkWorkOrderAsPaidCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.StartWorkOrderTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskProduct;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.repositories.ServiceRepository;
import com.andeva.atelier.platform.operations.domain.repositories.WorkOrderRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.MeasurementUnit;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.ServiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.OBD_SCAN;
import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.OIL_CHANGE;
import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.SEDAN_PLATE;
import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.SHIFT_NAME;
import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.VAN_PLATE;
import static com.andeva.atelier.platform.demo.DemoWorkshopSeeder.require;

/**
 * Fills the demo workshop with activity, so every screen has something to show: a history of paid and delivered work
 * orders (with parts consumed and their simulated vouchers), upcoming appointments (one raised from a predictive
 * alert), vehicle health reports, the mechanic's attendance of the last weeks and the payroll of the last fortnight.
 * <p>
 * Runs after {@link DemoWorkshopSeeder} and finds the workshop by the demo sedan's plate, so it also fills a workshop
 * seeded by an earlier version. Every block checks on its own whether it already ran, so restarts add nothing twice.
 * <p>
 * Past dates: appointments refuse the past and clock-ins older than 12 hours, and work orders stamp each step with the
 * current time. Attendance is therefore built straight from the domain, and the historical orders are created through
 * their real commands and then shifted back in time with a plain UPDATE.
 *
 * @author Joel Huamani Estefanero
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoActivitySeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoActivitySeeder.class);
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final BigDecimal IGV_FACTOR = new BigDecimal("1.18");
    private static final String BRAKE_PADS = "Cambio de pastillas de freno";
    private static final String BATTERY_SWAP = "Cambio de batería";
    private static final String RIO_PLATE = "AVN305";
    private static final String SENTRA_PLATE = "CDL928";
    private static final String LUCIA_DNI = "72345681";
    private static final String JORGE_DNI = "70123458";

    private final DemoSeedProperties props;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate readOnlyTx;
    private final UserRepository userRepository;
    private final TenantMembershipRepository membershipRepository;
    private final BranchQueryService branchQueryService;
    private final BranchCommandService branchCommandService;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerCommandService customerCommandService;
    private final VehicleCommandService vehicleCommandService;
    private final AppointmentCommandService appointmentCommandService;
    private final ServiceRepository serviceRepository;
    private final ServiceCommandService serviceCommandService;
    private final InventoryItemRepository inventoryItemRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderCommandService workOrderCommandService;
    private final SeriesConfigurationRepository seriesRepository;
    private final SeriesConfigurationCommandService seriesCommandService;
    private final ElectronicVoucherCommandService voucherCommandService;
    private final VoucherPaymentCommandService paymentCommandService;
    private final PredictiveAlertRepository alertRepository;
    private final PredictiveAlertCommandService alertCommandService;
    private final VehicleHealthReportRepository healthReportRepository;
    private final VehicleHealthReportCommandService healthReportCommandService;
    private final WorkShiftRepository workShiftRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final HaversineGeofencingService geofencingService;
    private final PayrollPaymentCommandService payrollCommandService;

    public DemoActivitySeeder(DemoSeedProperties props,
                              JdbcTemplate jdbc,
                              PlatformTransactionManager transactionManager,
                              UserRepository userRepository,
                              TenantMembershipRepository membershipRepository,
                              BranchQueryService branchQueryService,
                              BranchCommandService branchCommandService,
                              CustomerRepository customerRepository,
                              VehicleRepository vehicleRepository,
                              AppointmentRepository appointmentRepository,
                              CustomerCommandService customerCommandService,
                              VehicleCommandService vehicleCommandService,
                              AppointmentCommandService appointmentCommandService,
                              ServiceRepository serviceRepository,
                              ServiceCommandService serviceCommandService,
                              InventoryItemRepository inventoryItemRepository,
                              WorkOrderRepository workOrderRepository,
                              WorkOrderCommandService workOrderCommandService,
                              SeriesConfigurationRepository seriesRepository,
                              SeriesConfigurationCommandService seriesCommandService,
                              ElectronicVoucherCommandService voucherCommandService,
                              VoucherPaymentCommandService paymentCommandService,
                              PredictiveAlertRepository alertRepository,
                              PredictiveAlertCommandService alertCommandService,
                              VehicleHealthReportRepository healthReportRepository,
                              VehicleHealthReportCommandService healthReportCommandService,
                              WorkShiftRepository workShiftRepository,
                              AttendanceRecordRepository attendanceRepository,
                              HaversineGeofencingService geofencingService,
                              PayrollPaymentCommandService payrollCommandService) {
        this.props = props;
        this.jdbc = jdbc;
        this.readOnlyTx = new TransactionTemplate(transactionManager);
        this.readOnlyTx.setReadOnly(true);
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.branchQueryService = branchQueryService;
        this.branchCommandService = branchCommandService;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.appointmentRepository = appointmentRepository;
        this.customerCommandService = customerCommandService;
        this.vehicleCommandService = vehicleCommandService;
        this.appointmentCommandService = appointmentCommandService;
        this.serviceRepository = serviceRepository;
        this.serviceCommandService = serviceCommandService;
        this.inventoryItemRepository = inventoryItemRepository;
        this.workOrderRepository = workOrderRepository;
        this.workOrderCommandService = workOrderCommandService;
        this.seriesRepository = seriesRepository;
        this.seriesCommandService = seriesCommandService;
        this.voucherCommandService = voucherCommandService;
        this.paymentCommandService = paymentCommandService;
        this.alertRepository = alertRepository;
        this.alertCommandService = alertCommandService;
        this.healthReportRepository = healthReportRepository;
        this.healthReportCommandService = healthReportCommandService;
        this.workShiftRepository = workShiftRepository;
        this.attendanceRepository = attendanceRepository;
        this.geofencingService = geofencingService;
        this.payrollCommandService = payrollCommandService;
    }

    /** Everything the activity hangs on, read back from the workshop the base seeder created. */
    record Workshop(TenantId tenantId, BranchId branchId, TenantMembershipId ownerMembership,
                    TenantMembershipId mechanicMembership, WorkShift shift, Vehicle sedan, Vehicle van,
                    Map<String, Service> services, Map<String, InventoryItem> parts) {

        UUID mechanicId() {
            return mechanicMembership.value();
        }
    }

    private record PastPart(String sku, String quantity) { }

    private record PastTask(String service, String description, String price, String estimatedHours,
                            String actualHours, List<PastPart> parts) { }

    private record PastOrder(int daysAgo, String plate, int mileage, String diagnostic, PaymentMethod method,
                             List<PastTask> tasks) { }

    @Order(20)
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            seed();
        } catch (RuntimeException e) {
            log.error("Demo activity could not be seeded: {}", e.getMessage(), e);
        }
    }

    public void seed() {
        Optional<Workshop> found = findWorkshop();
        if (found.isEmpty()) {
            return;
        }
        Workshop workshop = found.get();
        step("work order history", () -> seedHistory(workshop));
        step("vehicle insights and appointments", () -> seedInsightsAndAppointments(workshop));
        step("attendance and payroll", () -> seedAttendanceAndPayroll(workshop));
    }

    private void step(String name, Runnable block) {
        try {
            block.run();
        } catch (RuntimeException e) {
            log.error("Demo {} could not be seeded: {}", name, e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Workshop lookup
    // ---------------------------------------------------------------------------------------------------------------

    private Optional<Workshop> findWorkshop() {
        if (!props.isComplete()) {
            return Optional.empty();
        }
        Optional<Vehicle> sedan = vehicleByPlate(SEDAN_PLATE);
        Optional<Vehicle> van = vehicleByPlate(VAN_PLATE);
        if (sedan.isEmpty() || van.isEmpty() || sedan.get().getCurrentOwnerId().isEmpty()) {
            log.warn("Demo activity skipped: the demo vehicles {} and {} do not exist", SEDAN_PLATE, VAN_PLATE);
            return Optional.empty();
        }
        CustomerId sedanOwner = sedan.get().getCurrentOwnerId().get();
        TenantId tenantId = read(() -> customerRepository.findById(sedanOwner)).orElseThrow().tenantId();
        Optional<TenantMembershipId> owner = membershipOf(tenantId, props.adminEmail());
        Optional<TenantMembershipId> mechanic = membershipOf(tenantId, props.mechanicEmail());
        Optional<WorkShift> shift = read(() -> workShiftRepository.findByTenantIdAndName(tenantId, SHIFT_NAME)
                .or(() -> workShiftRepository.findAllByTenantId(tenantId).stream().findFirst()));
        if (owner.isEmpty() || mechanic.isEmpty() || shift.isEmpty()) {
            log.warn("Demo activity skipped: owner, mechanic or shift of the demo workshop not found");
            return Optional.empty();
        }
        Branch branch = branchQueryService.handle(new GetBranchesByTenantIdQuery(tenantId)).getFirst();
        syncGeofenceRadius(branch);
        BranchId branchId = branch.id();

        Map<String, Service> services = new HashMap<>();
        read(() -> serviceRepository.findByTenantId(tenantId)).forEach(service -> services.put(service.getName(), service));
        services.computeIfAbsent(BRAKE_PADS, name -> require("service", serviceCommandService.handle(
                new CreateServiceItemCommand(tenantId, name, new BigDecimal("150.00"), "PEN", 60))));
        services.computeIfAbsent(BATTERY_SWAP, name -> require("service", serviceCommandService.handle(
                new CreateServiceItemCommand(tenantId, name, new BigDecimal("40.00"), "PEN", 20))));

        Map<String, InventoryItem> parts = new HashMap<>();
        for (String sku : List.of("OIL-5W30-1L", "FLT-OIL-YRS", "BRK-PAD-H1", "BAT-12V-75")) {
            read(() -> inventoryItemRepository.findByTenantIdAndSku(tenantId, Sku.of(sku))).ifPresent(item -> parts.put(sku, item));
        }
        return Optional.of(new Workshop(tenantId, branchId, owner.get(), mechanic.get(), shift.get(), sedan.get(),
                van.get(), services, parts));
    }

    /**
     * Repositories map lazy collections (ownership history, inventory batches…) to the domain, which needs an open
     * session: outside a command, every read goes through a read-only transaction.
     */
    private <T> T read(Supplier<T> query) {
        return readOnlyTx.execute(status -> query.get());
    }

    private Optional<Vehicle> vehicleByPlate(String plate) {
        return read(() -> vehicleRepository.findByPlate(LicensePlate.of(plate)));
    }

    /**
     * A branch seeded earlier keeps the radius it was created with: bring it to DEMO_BRANCH_RADIUS on every start,
     * keeping its name and location, so attendance can be tested from wherever the demo runs.
     */
    private void syncGeofenceRadius(Branch branch) {
        if (branch.geofenceRadiusMeters() == props.branchRadiusMeters()) {
            return;
        }
        GeoPoint location = branch.location() != null ? branch.location()
                : GeoPoint.of(props.branchLatitude(), props.branchLongitude());
        branchCommandService.handle(new UpdateBranchLocationCommand(branch.id(), branch.name(), branch.sunatCode(),
                location, props.branchRadiusMeters()));
        log.info("Demo branch geofence radius changed from {} m to {} m", branch.geofenceRadiusMeters(), props.branchRadiusMeters());
    }

    private Optional<TenantMembershipId> membershipOf(TenantId tenantId, String email) {
        return read(() -> userRepository.findByEmail(EmailAddress.of(email))
                .flatMap(user -> membershipRepository.findByTenantIdAndUserId(tenantId, user.id()))
                .map(membership -> TenantMembershipId.of(membership.id().value())));
    }

    // ---------------------------------------------------------------------------------------------------------------
    // 1. Work order history: two more customers and four closed orders over the last two months
    // ---------------------------------------------------------------------------------------------------------------

    private void seedHistory(Workshop w) {
        if (read(() -> customerRepository.existsByTenantIdAndTaxId(w.tenantId(), LUCIA_DNI))) {
            log.info("Demo work order history already exists: nothing to seed");
            return;
        }
        ensureSeries(w, VoucherType.BOLETA, "B001");
        ensureSeries(w, VoucherType.FACTURA, "F001");

        Customer lucia = require("customer", customerCommandService.handle(new RegisterIndividualCustomerCommand(
                w.tenantId(), "Lucía", "Mendoza Rojas", LUCIA_DNI, props.aliasOfAdmin("lucia"), "+51976543210")));
        Customer jorge = require("customer", customerCommandService.handle(new RegisterIndividualCustomerCommand(
                w.tenantId(), "Jorge", "Quispe Huamán", JORGE_DNI, props.aliasOfAdmin("jorge"), "+51965432109")));
        require("vehicle", vehicleCommandService.handle(new RegisterVehicleCommand(w.tenantId(), RIO_PLATE,
                "KNADN512AJ6123456", "Kia", "Rio", 2018, EngineType.GASOLINE, Optional.of(lucia.id()), Optional.empty())));
        require("vehicle", vehicleCommandService.handle(new RegisterVehicleCommand(w.tenantId(), SENTRA_PLATE,
                "3N1AB7AP7HY123456", "Nissan", "Sentra", 2017, EngineType.GASOLINE, Optional.of(jorge.id()), Optional.empty())));

        List<PastOrder> history = List.of(
                new PastOrder(62, SEDAN_PLATE, 41800, "Mantenimiento preventivo de los 40 000 km", PaymentMethod.CASH, List.of(
                        new PastTask(OIL_CHANGE, "Cambio de aceite sintético y filtro", "120.00", "0.75", "0.7",
                                List.of(new PastPart("OIL-5W30-1L", "4"), new PastPart("FLT-OIL-YRS", "1"))))),
                new PastOrder(35, RIO_PLATE, 58200, "Chirrido al frenar en las ruedas delanteras", PaymentMethod.DIGITAL_WALLET_YAPE, List.of(
                        new PastTask(BRAKE_PADS, "Cambio de pastillas delanteras y limpieza de calipers", "150.00", "1.0", "1.2",
                                List.of(new PastPart("BRK-PAD-H1", "1"))))),
                new PastOrder(21, SENTRA_PLATE, 73400, "Testigo de check engine encendido y consumo elevado", PaymentMethod.DEBIT_CARD, List.of(
                        new PastTask(OBD_SCAN, "Lectura de códigos: sensor de oxígeno con lectura lenta", "90.00", "0.5", "0.5", List.of()),
                        new PastTask(OIL_CHANGE, "Cambio de aceite vencido y filtro", "120.00", "0.75", "0.8",
                                List.of(new PastPart("OIL-5W30-1L", "4"), new PastPart("FLT-OIL-YRS", "1"))))),
                new PastOrder(9, RIO_PLATE, 59100, "El vehículo no arranca por las mañanas", PaymentMethod.CREDIT_CARD, List.of(
                        new PastTask(OBD_SCAN, "Prueba de carga: batería con 9,8 V en arranque", "90.00", "0.5", "0.4", List.of()),
                        new PastTask(BATTERY_SWAP, "Reemplazo de batería y limpieza de bornes", "40.00", "0.35", "0.3",
                                List.of(new PastPart("BAT-12V-75", "1"))))));

        for (PastOrder past : history) {
            seedPastOrder(w, past);
        }
        renumberOrdersByDate(w);
        log.info("Demo work order history seeded: {} paid and delivered orders", history.size());
    }

    /**
     * The past orders were numbered after the workshop's existing ones: number them again in the order they were
     * opened, so #1 is the oldest. Negative numbers first, to step around the (tenant, number) unique constraint.
     */
    private void renumberOrdersByDate(Workshop w) {
        jdbc.update("""
                UPDATE work_orders o SET internal_number = -ranked.position
                  FROM (SELECT id, row_number() OVER (ORDER BY created_at) AS position FROM work_orders WHERE tenant_id = ?) ranked
                 WHERE o.id = ranked.id""", w.tenantId().value());
        jdbc.update("UPDATE work_orders SET internal_number = -internal_number WHERE tenant_id = ? AND internal_number < 0",
                w.tenantId().value());
    }

    private void ensureSeries(Workshop w, VoucherType type, String serie) {
        // The "active" lookup locks the row (it serves correlative allocation), so only existence is checked here.
        // The correlative is the last one used: 0 makes the first voucher number 1
        if (!read(() -> seriesRepository.existsByTenantIdAndBranchIdAndSerie(w.tenantId(), w.branchId(), VoucherSerie.of(serie)))) {
            seriesCommandService.handle(new ConfigureSeriesCommand(w.tenantId(), w.branchId(), type, VoucherSerie.of(serie), 0));
        }
    }

    private void seedPastOrder(Workshop w, PastOrder past) {
        Vehicle vehicle = vehicleByPlate(past.plate()).orElseThrow();
        CustomerId customerId = vehicle.getCurrentOwnerId().orElseThrow();
        WorkOrder order = require("past work order", workOrderCommandService.handle(new CreateWorkOrderCommand(
                w.tenantId(), w.branchId(), null, vehicle.id(), customerId, Mileage.of(past.mileage()),
                DiagnosticSummary.of(past.diagnostic()))));

        for (PastTask task : past.tasks()) {
            order = require("past task", workOrderCommandService.handle(new AddTaskToWorkOrderCommand(order.getId(),
                    w.services().get(task.service()).getId(), w.mechanicId(), task.description(),
                    new BigDecimal(task.price()), new BigDecimal(task.estimatedHours()), "PEN")));
            WorkOrderTask added = order.getTasks().getLast();
            for (PastPart part : task.parts()) {
                InventoryItem item = w.parts().get(part.sku());
                if (item != null) {
                    require("part on task", workOrderCommandService.handle(new AddProductToTaskCommand(added.getId(),
                            item.getId().value(), new BigDecimal(part.quantity()), null, "PEN")));
                }
            }
        }
        order = reload(order);
        for (int i = 0; i < order.getTasks().size(); i++) {
            WorkOrderTask task = order.getTasks().get(i);
            require("task start", workOrderCommandService.handle(new StartWorkOrderTaskCommand(task.getId())));
            require("task completion", workOrderCommandService.handle(new CompleteWorkOrderTaskCommand(task.getId(),
                    new BigDecimal(past.tasks().get(i).actualHours()), "Trabajo terminado y probado")));
        }

        // Simulated voucher (Nubefact runs in demo mode), paid in full: the payment marks the order as paid
        order = reload(order);
        ElectronicVoucher voucher = voucherCommandService.handle(new IssueElectronicVoucherCommand(w.tenantId(),
                w.branchId(), customerId, Optional.of(WorkOrderId.of(order.getId().value())), VoucherType.BOLETA,
                VoucherSerie.of("B001"), new CustomerFiscalInfo(null, "", null, null), Currency.PEN, voucherLines(w, order)));
        paymentCommandService.handle(new RegisterVoucherPaymentCommand(voucher.getId(), w.tenantId(), w.branchId(),
                voucher.getTotalAmount(), past.method(), "DEMO-" + order.getInternalNumber().sequence()));
        if (reload(order).getStatus() == WorkOrderStatus.COMPLETED) {
            require("payment", workOrderCommandService.handle(new MarkWorkOrderAsPaidCommand(order.getId())));
        }
        require("delivery", workOrderCommandService.handle(new DeliverVehicleCommand(order.getId())));

        backdate(order, past);
    }

    private WorkOrder reload(WorkOrder order) {
        return read(() -> workOrderRepository.findById(order.getId())).orElseThrow();
    }

    private List<VoucherLineCommandDto> voucherLines(Workshop w, WorkOrder order) {
        Map<UUID, String> partNames = new HashMap<>();
        w.parts().values().forEach(item -> partNames.put(item.getId().value(), item.getName()));
        List<VoucherLineCommandDto> lines = new ArrayList<>();
        for (WorkOrderTask task : order.getTasks()) {
            lines.add(VoucherLineCommandDto.of(null, VoucherItemType.SERVICE, task.getDescription(),
                    Quantity.ofUnits(1), withIgv(task.getPrice())));
            for (WorkOrderTaskProduct product : task.getConsumedProducts()) {
                lines.add(VoucherLineCommandDto.of(product.getProductId(), VoucherItemType.PRODUCT,
                        partNames.getOrDefault(product.getProductId(), "Repuesto"),
                        Quantity.of(product.getQuantity().value(), MeasurementUnit.UNIT), withIgv(product.getUnitPrice())));
            }
        }
        return lines;
    }

    private static Money withIgv(Money net) {
        return Money.soles(net.amount().multiply(IGV_FACTOR).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Moves the order, its tasks, voucher and payment to the past: received at 9:00 Lima {@code daysAgo} days ago,
     * tasks run back to back from 9:30, billed and paid half an hour after the last task.
     */
    private void backdate(WorkOrder order, PastOrder past) {
        // Every row was written seconds ago, so shifting it by (now - target) lands it on the target moment,
        // whatever the column type (timestamp or timestamptz) and the session time zone
        Instant openedAt = LocalDate.now(LIMA).minusDays(past.daysAgo()).atTime(9, 0).atZone(LIMA).toInstant();
        double shift = Duration.between(openedAt, Instant.now()).toSeconds();
        UUID orderId = order.getId().value();

        long minutes = 30;
        for (int i = 0; i < order.getTasks().size(); i++) {
            long worked = Math.round(Double.parseDouble(past.tasks().get(i).actualHours()) * 60);
            jdbc.update("""
                    UPDATE work_order_tasks
                       SET created_at   = created_at - make_interval(secs => ?),
                           started_at   = created_at - make_interval(secs => ?) + make_interval(mins => ?),
                           completed_at = created_at - make_interval(secs => ?) + make_interval(mins => ?),
                           updated_at   = created_at - make_interval(secs => ?) + make_interval(mins => ?)
                     WHERE id = ?""",
                    shift, shift, (int) minutes, shift, (int) (minutes + worked), shift, (int) (minutes + worked),
                    order.getTasks().get(i).getId().value());
            minutes += worked;
        }
        int billedAfter = (int) minutes + 30;
        double billShift = shift - billedAfter * 60.0;
        jdbc.update("""
                UPDATE work_orders
                   SET created_at = created_at - make_interval(secs => ?),
                       updated_at = created_at - make_interval(secs => ?) + make_interval(mins => ?)
                 WHERE id = ?""", shift, shift, billedAfter, orderId);
        jdbc.update("""
                UPDATE electronic_vouchers
                   SET created_at = created_at - make_interval(secs => ?), updated_at = updated_at - make_interval(secs => ?)
                 WHERE work_order_id = ?""", billShift, billShift, orderId);
        jdbc.update("""
                UPDATE voucher_payments
                   SET paid_at = paid_at - make_interval(secs => ?), created_at = created_at - make_interval(secs => ?),
                       updated_at = updated_at - make_interval(secs => ?)
                 WHERE voucher_id IN (SELECT id FROM electronic_vouchers WHERE work_order_id = ?)""",
                billShift, billShift, billShift, orderId);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // 2 and 3. Vehicle insights and upcoming appointments
    // ---------------------------------------------------------------------------------------------------------------

    private void seedInsightsAndAppointments(Workshop w) {
        VehicleId sedanId = w.sedan().id();
        for (Vehicle vehicle : List.of(w.van(), w.sedan())) {
            if (read(() -> healthReportRepository.findLatestByTenantIdAndVehicleId(w.tenantId(), vehicle.id())).isEmpty()) {
                try {
                    healthReportCommandService.handle(new GenerateVehicleHealthReportCommand(w.tenantId(), vehicle.id(), 30, true));
                } catch (RuntimeException e) {
                    // The report needs the AI provider: without it the demo still works, the report is generated live
                    log.warn("Demo health report for {} not generated: {}", vehicle.id().value(), e.getMessage());
                }
            }
        }

        if (!read(() -> appointmentRepository.findByVehicleId(sedanId)).isEmpty()) {
            log.info("Demo appointments already exist: nothing to seed");
            return;
        }

        // The sedan's oil is due: the predictive alert turns into a confirmed appointment on the real branch
        AlertId oilAlert = alertCommandService.handle(new GeneratePredictiveAlertCommand(sedanId, w.tenantId(),
                Optional.of(ServiceId.of(w.services().get(OIL_CHANGE).getId().value())), AlertType.OIL_LIFE_DEPLETION,
                ConfidenceScore.of(88), "Vida útil del aceite al 12 %: cambio recomendado en los próximos 500 km"));
        LocalDate first = nextWorkday(LocalDate.now(LIMA));
        appointment(w, w.sedan(), first, LocalTime.of(9, 0), 45,
                "Cambio de aceite y filtro recomendado por la alerta predictiva del OBD-II", true);
        alertCommandService.handle(new AcknowledgePredictiveAlertCommand(oilAlert));

        vehicleByPlate(RIO_PLATE).ifPresent(rio ->
                appointment(w, rio, first, LocalTime.of(11, 30), 60,
                        "Revisión de garantía de las pastillas de freno cambiadas el mes pasado", false));
        vehicleByPlate(SENTRA_PLATE).ifPresent(sentra ->
                appointment(w, sentra, nextWorkday(first), LocalTime.of(10, 0), 90,
                        "Cambio del sensor de oxígeno diagnosticado en la visita anterior", true));
        appointment(w, w.van(), nextWorkday(nextWorkday(first)), LocalTime.of(15, 0), 30,
                "Control posterior a la reparación del sistema de refrigeración", false);
        log.info("Demo appointments seeded starting {}", first);
    }

    private void appointment(Workshop w, Vehicle vehicle, LocalDate day, LocalTime time, int minutes, String reason,
                             boolean confirmed) {
        Appointment appointment = require("appointment", appointmentCommandService.handle(new ScheduleAppointmentCommand(
                w.tenantId(), w.branchId(), vehicle.getCurrentOwnerId().orElseThrow(), vehicle.id(),
                day.atTime(time).atZone(LIMA).toInstant(), minutes, reason)));
        if (confirmed) {
            require("appointment confirmation", appointmentCommandService.handle(
                    new ConfirmAppointmentCommand(w.tenantId(), appointment.id())));
        }
    }

    private static LocalDate nextWorkday(LocalDate day) {
        LocalDate next = day.plusDays(1);
        return next.getDayOfWeek() == DayOfWeek.SUNDAY ? next.plusDays(1) : next;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // 4. Attendance since the start of the last closed fortnight, and that fortnight's payroll
    // ---------------------------------------------------------------------------------------------------------------

    private void seedAttendanceAndPayroll(Workshop w) {
        LocalDate today = LocalDate.now(LIMA);
        LocalDate periodStart = today.getDayOfMonth() > 15 ? today.withDayOfMonth(1) : today.minusMonths(1).withDayOfMonth(16);
        LocalDate periodEnd = today.getDayOfMonth() > 15 ? today.withDayOfMonth(15) : periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        Instant since = periodStart.atStartOfDay(LIMA).toInstant();
        if (read(() -> attendanceRepository.findAllByMembershipAndPeriod(w.tenantId(), w.mechanicMembership(), since, Instant.now())).isEmpty()) {
            int days = seedAttendance(w, periodStart, today.minusDays(1));
            log.info("Demo attendance seeded: {} working days since {}", days, periodStart);
        }

        log.info("Demo payroll {} to {}: {}", periodStart, periodEnd, generatePayroll(w, periodStart, periodEnd));
    }

    private int seedAttendance(Workshop w, LocalDate from, LocalDate to) {
        Random random = new Random(from.toEpochDay());
        GeoCoordinates branch = GeoCoordinates.of(props.branchLatitude(), props.branchLongitude());
        int days = 0;
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            if (day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            // Mostly on time; every eighth day late, and one day of overtime a week
            int arrival = days % 8 == 5 ? 22 + random.nextInt(15) : -12 + random.nextInt(18);
            int leave = day.getDayOfWeek() == DayOfWeek.THURSDAY ? 95 + random.nextInt(20) : random.nextInt(25);
            Instant in = day.atTime(8, 0).plusMinutes(arrival).atZone(LIMA).toInstant();
            Instant out = day.atTime(17, 0).plusMinutes(leave).atZone(LIMA).toInstant();
            GeoCoordinates location = GeoCoordinates.of(props.branchLatitude() + (random.nextDouble() - 0.5) * 0.0004,
                    props.branchLongitude() + (random.nextDouble() - 0.5) * 0.0004);

            AttendanceRecord record = AttendanceRecord.recordClockIn(w.tenantId(), w.branchId(), w.mechanicMembership(),
                    w.shift(), location, branch, props.branchRadiusMeters(), geofencingService, LIMA, in);
            record.recordClockOut(out, w.shift(), LIMA);
            attendanceRepository.save(record);
            days++;
        }
        return days;
    }

    /** Generates and approves the fortnight's payroll; an existing one is left as is. */
    private String generatePayroll(Workshop w, LocalDate start, LocalDate end) {
        var generated = payrollCommandService.handle(new GeneratePayrollCommand(w.tenantId(), w.mechanicMembership(), start, end));
        if (generated.isFailure()) {
            return "not generated (" + generated.getError().message() + ")";
        }
        PayrollPayment payroll = generated.getOrThrow();
        var approved = payrollCommandService.handle(new ApprovePayrollCommand(w.tenantId(), payroll.getId(), w.ownerMembership()));
        return approved.isSuccess() ? "generated and approved" : "generated, approval failed (" + approved.getError().message() + ")";
    }
}
