package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.TransferVehicleOwnershipCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.domain.services.VehicleTransferDomainService;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying {@link VehicleCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleCommandServiceImpl Unit Tests")
class VehicleCommandServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SubscriptionValidationService subscriptionValidationService;

    private VehicleTransferDomainService vehicleTransferDomainService;

    private VehicleCommandServiceImpl service;

    private TenantId sampleTenantId;
    private CustomerId sampleCustomerId;

    @BeforeEach
    void setUp() {
        vehicleTransferDomainService = new VehicleTransferDomainService();
        service = new VehicleCommandServiceImpl(
                vehicleRepository,
                customerRepository,
                subscriptionValidationService,
                vehicleTransferDomainService
        );
        sampleTenantId = TenantId.generate();
        sampleCustomerId = CustomerId.generate();
    }

    @Test
    @DisplayName("Should successfully register vehicle when data and quota are valid")
    void shouldRegisterVehicleSuccessfully() {
        RegisterVehicleCommand command = new RegisterVehicleCommand(
                sampleTenantId,
                "ABC123",
                "1HGCR2F83HA000000",
                "Honda",
                "Accord",
                2022,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        when(vehicleRepository.existsByPlate(any(LicensePlate.class))).thenReturn(false);
        when(vehicleRepository.existsByVin("1HGCR2F83HA000000")).thenReturn(false);
        Customer customer = Customer.registerIndividual(
                sampleCustomerId,
                sampleTenantId,
                PersonName.of("Carlos", "Rios"),
                TaxId.of("45871234"),
                EmailAddress.of("carlos.rios@example.pe"),
                PhoneNumber.of("+51987654321")
        );
        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(customer));
        when(subscriptionValidationService.validateVehicleQuota(sampleTenantId.value())).thenReturn(true);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Vehicle saved = result.toOptional().orElseThrow();
        assertThat(saved.plate().value()).isEqualTo("ABC123");
        assertThat(saved.brand()).isEqualTo("Honda");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should reject vehicle registration when license plate already exists")
    void shouldRejectWhenPlateAlreadyExists() {
        RegisterVehicleCommand command = new RegisterVehicleCommand(
                sampleTenantId,
                "ABC123",
                null,
                "Toyota",
                "Yaris",
                2020,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        when(vehicleRepository.existsByPlate(any(LicensePlate.class))).thenReturn(true);

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("already exists");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject vehicle registration when VIN already exists")
    void shouldRejectWhenVinAlreadyExists() {
        RegisterVehicleCommand command = new RegisterVehicleCommand(
                sampleTenantId,
                "XYZ789",
                "1HGCR2F83HA999999",
                "Toyota",
                "Yaris",
                2020,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        when(vehicleRepository.existsByPlate(any(LicensePlate.class))).thenReturn(false);
        when(vehicleRepository.existsByVin("1HGCR2F83HA999999")).thenReturn(true);

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("VIN");
    }

    @Test
    @DisplayName("Should reject vehicle registration when vehicle quota is exceeded")
    void shouldRejectWhenQuotaExceeded() {
        RegisterVehicleCommand command = new RegisterVehicleCommand(
                sampleTenantId,
                "ABC999",
                null,
                "Kia",
                "Rio",
                2021,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        when(vehicleRepository.existsByPlate(any(LicensePlate.class))).thenReturn(false);
        Customer customer = Customer.registerIndividual(
                sampleCustomerId,
                sampleTenantId,
                PersonName.of("Carlos", "Rios"),
                TaxId.of("45871234"),
                EmailAddress.of("carlos.rios@example.pe"),
                PhoneNumber.of("+51987654321")
        );
        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(customer));
        when(subscriptionValidationService.validateVehicleQuota(sampleTenantId.value())).thenReturn(false);

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("quota exceeded");
    }

    @Test
    @DisplayName("Should transfer vehicle ownership successfully")
    void shouldTransferOwnershipSuccessfully() {
        VehicleId vehicleId = VehicleId.generate();
        CustomerId newCustomerId = CustomerId.generate();

        Vehicle vehicle = Vehicle.register(
                vehicleId,
                LicensePlate.of("ABC123"),
                null,
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        Customer newCustomer = Customer.registerIndividual(
                newCustomerId,
                sampleTenantId,
                PersonName.of("Laura", "Perez"),
                TaxId.of("87654321"),
                EmailAddress.of("laura.perez@example.pe"),
                PhoneNumber.of("+51987654321")
        );

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndTenantId(newCustomerId, sampleTenantId))
                .thenReturn(Optional.of(newCustomer));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        TransferVehicleOwnershipCommand command = new TransferVehicleOwnershipCommand(
                sampleTenantId,
                vehicleId,
                newCustomerId,
                LocalDate.now()
        );

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(vehicle.getActiveOwnership().get().getCustomerId()).isEqualTo(newCustomerId);
    }

    @Test
    @DisplayName("Should reject transfer when vehicle is not found")
    void shouldRejectTransferWhenVehicleNotFound() {
        VehicleId vehicleId = VehicleId.generate();
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        TransferVehicleOwnershipCommand command = new TransferVehicleOwnershipCommand(
                sampleTenantId,
                vehicleId,
                sampleCustomerId,
                LocalDate.now()
        );

        Result<Vehicle, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("not found");
    }
}
