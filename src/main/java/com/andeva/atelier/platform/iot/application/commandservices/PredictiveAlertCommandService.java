package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ConvertAlertToAppointmentCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.DismissPredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.GeneratePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

import java.util.UUID;

/**
 * Command Service for managing predictive alert lifecycle, acknowledgments, dismissals, and appointment conversion.
 *
 * @author Joel Huamani Estefanero
 */
public interface PredictiveAlertCommandService {

    AlertId handle(GeneratePredictiveAlertCommand command);

    void handle(AcknowledgePredictiveAlertCommand command);

    void handle(DismissPredictiveAlertCommand command);

    UUID handle(ConvertAlertToAppointmentCommand command);
}
