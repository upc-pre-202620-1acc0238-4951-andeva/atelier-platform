package com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.DailyReconciliationResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.VoucherPaymentResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resource Assembler transforming {@link VoucherPayment} child entities into REST presentation resources.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VoucherPaymentResourceAssembler {

    public VoucherPaymentResource toResource(VoucherPayment payment) {
        if (payment == null) {
            return null;
        }
        return new VoucherPaymentResource(
                payment.getId().value(),
                payment.getVoucherId().value(),
                payment.getAmount().amount(),
                payment.getAmount().currency().name(),
                payment.getPaymentMethod().name(),
                payment.getTransactionReference(),
                payment.getStatus().name(),
                payment.getPaidAt()
        );
    }

    public List<VoucherPaymentResource> toResourceList(List<VoucherPayment> payments) {
        if (payments == null) {
            return List.of();
        }
        return payments.stream().map(this::toResource).toList();
    }

    public DailyReconciliationResource toDailyReconciliationResource(UUID branchId, LocalDate date, List<VoucherPayment> payments) {
        List<VoucherPaymentResource> paymentResources = new ArrayList<>();
        BigDecimal totalCollected = BigDecimal.ZERO;
        Map<String, BigDecimal> breakdown = new HashMap<>();

        if (payments != null) {
            for (VoucherPayment p : payments) {
                paymentResources.add(toResource(p));
                BigDecimal amount = p.getAmount().amount();
                totalCollected = totalCollected.add(amount);

                String method = p.getPaymentMethod().name();
                breakdown.merge(method, amount, BigDecimal::add);
            }
        }

        return new DailyReconciliationResource(
                branchId,
                date,
                totalCollected,
                "PEN",
                paymentResources.size(),
                breakdown,
                paymentResources
        );
    }
}
