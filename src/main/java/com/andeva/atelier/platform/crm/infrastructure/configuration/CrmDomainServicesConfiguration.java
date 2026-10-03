package com.andeva.atelier.platform.crm.infrastructure.configuration;

import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.services.AppointmentSchedulingService;
import com.andeva.atelier.platform.crm.domain.services.VehicleTransferDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration declaring domain services as managed beans without polluting domain layer.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class CrmDomainServicesConfiguration {

    @Bean
    public VehicleTransferDomainService vehicleTransferDomainService() {
        return new VehicleTransferDomainService();
    }

    @Bean
    public AppointmentSchedulingService appointmentSchedulingService(AppointmentRepository appointmentRepository) {
        return new AppointmentSchedulingService(appointmentRepository);
    }
}
