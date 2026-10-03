package com.andeva.atelier.platform.crm.infrastructure.persistence;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleOwnershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End QA Integration Test persisting CRM domain aggregates to real PostgreSQL database.
 *
 * @author Adiel Sanchez Santin
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/atelier_db",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=update"
})
@DisplayName("QA Database Integration Test for CRM & Fleet Management")
class CrmDatabasePersistenceIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleOwnershipRepository vehicleOwnershipRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    @Transactional
    @DisplayName("QA E2E Flow: Register Individual and Company Customers, Vehicle, Ownership, Appointment and Arrive")
    void shouldExecuteCompleteCrmFlowWithRealDatabasePersistence() {
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        BranchId branchId = BranchId.of(UUID.randomUUID());

        // 1. TC-CRM-001: Register Individual Customer
        CustomerId individualId = CustomerId.generate();
        Customer individual = Customer.registerIndividual(
                individualId,
                tenantId,
                PersonName.of("Carlos", "Mendoza"),
                TaxId.of("47891234"),
                EmailAddress.of("carlos.mendoza." + UUID.randomUUID().toString().substring(0, 8) + "@example.com"),
                PhoneNumber.of("+51987654321")
        );
        Customer savedIndividual = customerRepository.save(individual);
        assertThat(savedIndividual.id()).isEqualTo(individualId);

        Optional<Customer> loadedIndividual = customerRepository.findByIdAndTenantId(individualId, tenantId);
        assertThat(loadedIndividual).isPresent();
        assertThat(loadedIndividual.get().type()).isEqualTo(CustomerType.INDIVIDUAL);
        assertThat(loadedIndividual.get().status()).isEqualTo(CustomerStatus.ACTIVE);

        // 2. TC-CRM-002: Register Corporate Company Customer (SUNAT valid RUC)
        CustomerId companyId = CustomerId.generate();
        Customer company = Customer.registerCompany(
                companyId,
                tenantId,
                "Transportes del Sur S.A.C.",
                TaxId.of("20131312955"),
                EmailAddress.of("ops." + UUID.randomUUID().toString().substring(0, 8) + "@transur.pe"),
                PhoneNumber.of("+5114567890")
        );
        Customer savedCompany = customerRepository.save(company);
        assertThat(savedCompany.id()).isEqualTo(companyId);

        Optional<Customer> loadedCompany = customerRepository.findByTenantIdAndTaxId(tenantId, "20131312955");
        assertThat(loadedCompany).isPresent();
        assertThat(loadedCompany.get().companyName()).isEqualTo("Transportes del Sur S.A.C.");

        // 3. TC-CRM-003: Register Vehicle with Initial Ownership to Individual
        VehicleId vehicleId = VehicleId.generate();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        LicensePlate plate = LicensePlate.of("ABC" + uniqueSuffix.substring(0, 3));
        Vin vin = Vin.of("1HGCR2F83HA" + uniqueSuffix + "12");

        Vehicle vehicle = Vehicle.register(
                vehicleId,
                plate,
                vin,
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(individualId),
                Optional.empty()
        );
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        assertThat(savedVehicle.id()).isEqualTo(vehicleId);

        Optional<Vehicle> loadedVehicle = vehicleRepository.findByPlate(plate);
        assertThat(loadedVehicle).isPresent();
        assertThat(loadedVehicle.get().brand()).isEqualTo("Toyota");

        Optional<VehicleOwnership> activeOwnership = vehicleOwnershipRepository.findActiveOwnershipByVehicleId(vehicleId);
        assertThat(activeOwnership).isPresent();
        assertThat(activeOwnership.get().getCustomerId()).isEqualTo(individualId);
        assertThat(activeOwnership.get().isCurrent()).isTrue();

        // 4. TC-CRM-004: Schedule Appointment
        AppointmentId appointmentId = AppointmentId.generate();
        Instant scheduledTime = Instant.now().plus(24, ChronoUnit.HOURS);
        Appointment appointment = Appointment.schedule(
                appointmentId,
                tenantId,
                branchId,
                individualId,
                vehicleId,
                scheduledTime,
                60,
                "Mantenimiento periodico de 20,000 km"
        );
        Appointment savedAppointment = appointmentRepository.save(appointment);
        assertThat(savedAppointment.id()).isEqualTo(appointmentId);
        assertThat(savedAppointment.status()).isEqualTo(AppointmentStatus.PENDING);

        // 5. TC-CRM-005: Confirm and Mark Arrived
        savedAppointment.confirm();
        savedAppointment.markArrived();
        appointmentRepository.save(savedAppointment);

        Optional<Appointment> loadedAppointment = appointmentRepository.findByIdAndTenantId(appointmentId, tenantId);
        assertThat(loadedAppointment).isPresent();
        assertThat(loadedAppointment.get().status()).isEqualTo(AppointmentStatus.ARRIVED);

        // 6. TC-CRM-006: Transfer Vehicle Ownership from Individual to Company
        LocalDate transferDate = LocalDate.now();
        savedVehicle.transferOwnership(companyId, transferDate);
        vehicleRepository.save(savedVehicle);

        List<VehicleOwnership> history = vehicleOwnershipRepository.findByVehicleId(vehicleId);
        assertThat(history).hasSize(2);

        Optional<VehicleOwnership> newActiveOwnership = vehicleOwnershipRepository.findActiveOwnershipByVehicleId(vehicleId);
        assertThat(newActiveOwnership).isPresent();
        assertThat(newActiveOwnership.get().getCustomerId()).isEqualTo(companyId);
        assertThat(newActiveOwnership.get().isCurrent()).isTrue();
    }
}
