package com.andeva.atelier.platform.iot.application;

import com.andeva.atelier.platform.iot.application.commandservices.VehicleFaultCommandService;
import com.andeva.atelier.platform.iot.application.internal.commandservices.VehicleFaultCommandServiceImpl;
import com.andeva.atelier.platform.iot.domain.exceptions.VehicleFaultNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.VehicleFault;
import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ResolveVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.entities.DtcCatalogEntry;
import com.andeva.atelier.platform.iot.domain.model.enums.DtcCategory;
import com.andeva.atelier.platform.iot.domain.model.enums.FaultSeverity;
import com.andeva.atelier.platform.iot.domain.model.events.VehicleFaultDetectedEvent;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DtcCode;
import com.andeva.atelier.platform.iot.domain.repositories.DtcCatalogRepository;
import com.andeva.atelier.platform.iot.domain.repositories.VehicleFaultRepository;
import com.andeva.atelier.platform.iot.domain.services.DtcCodeEvaluationService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link VehicleFaultCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
class VehicleFaultCommandServiceTest {

    @Mock
    private VehicleFaultRepository vehicleFaultRepository;
    @Mock
    private DtcCatalogRepository dtcCatalogRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DtcCodeEvaluationService evaluationService;
    private VehicleFaultCommandService service;

    private final TenantId tenantId = TenantId.generate();
    private final VehicleId vehicleId = VehicleId.generate();
    private final DtcCode dtcCode = DtcCode.of("P0300");

    @BeforeEach
    void setUp() {
        evaluationService = new DtcCodeEvaluationService();
        service = new VehicleFaultCommandServiceImpl(
                vehicleFaultRepository,
                dtcCatalogRepository,
                evaluationService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should register vehicle fault using metadata from DTC catalog")
    void shouldRegisterVehicleFaultUsingCatalog() {
        DtcCatalogEntry catalogEntry = DtcCatalogEntry.register(
                dtcCode,
                DtcCategory.POWERTRAIN_P,
                "Random/Multiple Cylinder Misfire Detected",
                FaultSeverity.CRITICAL
        );
        when(dtcCatalogRepository.findByCode(dtcCode)).thenReturn(Optional.of(catalogEntry));

        RegisterVehicleFaultCommand command = new RegisterVehicleFaultCommand(
                vehicleId,
                tenantId,
                dtcCode,
                FaultSeverity.CRITICAL,
                ""
        );

        FaultId faultId = service.handle(command);
        assertThat(faultId).isNotNull();

        ArgumentCaptor<VehicleFault> captor = ArgumentCaptor.forClass(VehicleFault.class);
        verify(vehicleFaultRepository).save(captor.capture());

        VehicleFault saved = captor.getValue();
        assertThat(saved.getDtcCode()).isEqualTo(dtcCode);
        assertThat(saved.getSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        assertThat(saved.getDescription()).isEqualTo("Random/Multiple Cylinder Misfire Detected");
        assertThat(saved.isResolved()).isFalse();

        verify(eventPublisher, atLeastOnce()).publishEvent(any(VehicleFaultDetectedEvent.class));
    }

    @Test
    @DisplayName("Should fall back to DtcCodeEvaluationService when code is not in catalog")
    void shouldFallBackToEvaluationServiceWhenNotInCatalog() {
        DtcCode uncataloged = DtcCode.of("P0217");
        when(dtcCatalogRepository.findByCode(uncataloged)).thenReturn(Optional.empty());

        RegisterVehicleFaultCommand command = new RegisterVehicleFaultCommand(
                vehicleId,
                tenantId,
                uncataloged,
                FaultSeverity.CRITICAL,
                "Engine Coolant Over Temperature"
        );

        FaultId faultId = service.handle(command);
        assertThat(faultId).isNotNull();

        ArgumentCaptor<VehicleFault> captor = ArgumentCaptor.forClass(VehicleFault.class);
        verify(vehicleFaultRepository).save(captor.capture());

        VehicleFault saved = captor.getValue();
        assertThat(saved.getSeverity()).isEqualTo(FaultSeverity.CRITICAL);
        assertThat(saved.getDescription()).isEqualTo("Engine Coolant Over Temperature");
    }

    @Test
    @DisplayName("Should resolve active vehicle fault")
    void shouldResolveActiveVehicleFault() {
        FaultId faultId = FaultId.generate();
        VehicleFault fault = VehicleFault.detect(
                vehicleId, tenantId, dtcCode, FaultSeverity.CRITICAL, "Cylinder Misfire"
        );
        when(vehicleFaultRepository.findById(faultId)).thenReturn(Optional.of(fault));

        ResolveVehicleFaultCommand command = new ResolveVehicleFaultCommand(faultId, "Replaced ignition coils");
        service.handle(command);

        assertThat(fault.isResolved()).isTrue();
        assertThat(fault.getResolvedAt()).isPresent();

        verify(vehicleFaultRepository).save(fault);
    }

    @Test
    @DisplayName("Should throw VehicleFaultNotFoundException when resolving non-existent fault")
    void shouldThrowWhenResolvingNonExistentFault() {
        FaultId faultId = FaultId.generate();
        when(vehicleFaultRepository.findById(faultId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.handle(new ResolveVehicleFaultCommand(faultId, "notes")))
                .isInstanceOf(VehicleFaultNotFoundException.class);
    }
}
