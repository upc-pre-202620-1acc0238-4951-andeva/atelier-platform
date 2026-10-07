package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.ElectronicVoucherPersistenceEntity;
import com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities.VoucherPaymentPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Persistence Assembler converting bidirectionally between {@link VoucherPayment} entity and {@link VoucherPaymentPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VoucherPaymentPersistenceAssembler {

    public VoucherPaymentPersistenceEntity toEntity(VoucherPayment domain, ElectronicVoucherPersistenceEntity voucherEntity) {
        if (domain == null) {
            return null;
        }
        Objects.requireNonNull(voucherEntity, "Parent ElectronicVoucherPersistenceEntity cannot be null");

        return new VoucherPaymentPersistenceEntity(
                domain.getId().value(),
                voucherEntity,
                domain.getTenantId().value(),
                domain.getBranchId().value(),
                domain.getAmount().amount(),
                domain.getAmount().currency().name(),
                domain.getPaymentMethod().name(),
                domain.getTransactionReference(),
                domain.getStatus().name(),
                domain.getPaidAt()
        );
    }

    public VoucherPayment toDomain(VoucherPaymentPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        Currency currency = Currency.valueOf(entity.getCurrency() != null ? entity.getCurrency() : "PEN");
        PaymentMethod method = PaymentMethod.valueOf(entity.getPaymentMethod());
        PaymentStatus status = PaymentStatus.valueOf(entity.getStatus());

        UUID vId = (entity.getVoucher() != null && entity.getVoucher().getId() != null)
                ? entity.getVoucher().getId()
                : (entity.getId() != null ? entity.getId() : UUID.randomUUID());

        return new VoucherPayment(
                PaymentId.of(entity.getId()),
                VoucherId.of(vId),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                Money.of(entity.getAmount(), currency),
                method,
                entity.getTransactionReference(),
                status,
                entity.getPaidAt()
        );
    }
}
