package com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.hr.domain.model.aggregates.PayrollPayment;
import com.andeva.atelier.platform.hr.domain.model.entities.PayrollItem;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollItemId;
import com.andeva.atelier.platform.hr.domain.model.ids.PayrollPaymentId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.PayPeriod;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.PayrollItemPersistenceEntity;
import com.andeva.atelier.platform.hr.infrastructure.persistence.jpa.entities.PayrollPaymentPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PayrollPaymentPersistenceAssembler {

    public PayrollPayment toDomain(PayrollPaymentPersistenceEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.valueOf(entity.getCurrency().toUpperCase());
        Money baseAmount = Money.of(entity.getBaseAmount(), currency);
        Money deductions = Money.of(entity.getDeductions(), currency);
        Money bonuses = Money.of(entity.getBonuses(), currency);
        Money totalPaid = Money.of(entity.getTotalPaid(), currency);

        List<PayrollItem> items = new ArrayList<>();
        if (entity.getItems() != null) {
            for (PayrollItemPersistenceEntity itemEntity : entity.getItems()) {
                items.add(new PayrollItem(
                        PayrollItemId.of(itemEntity.getId()),
                        PayrollPaymentId.of(entity.getId()),
                        itemEntity.getCategory(),
                        itemEntity.getConcept(),
                        Money.of(itemEntity.getAmount(), currency),
                        itemEntity.getType(),
                        itemEntity.getDate()
                ));
            }
        }

        return new PayrollPayment(
                PayrollPaymentId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                TenantMembershipId.of(entity.getMembershipId()),
                PayPeriod.of(entity.getPeriodStart(), entity.getPeriodEnd()),
                baseAmount,
                deductions,
                bonuses,
                totalPaid,
                entity.getStatus(),
                entity.getPaidAt(),
                entity.getPaymentReference(),
                items
        );
    }

    public PayrollPaymentPersistenceEntity toEntity(PayrollPayment domain) {
        if (domain == null) return null;

        PayrollPaymentPersistenceEntity entity = new PayrollPaymentPersistenceEntity(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setMembershipId(domain.getMembershipId().value());
        entity.setPeriodStart(domain.getPeriod().startDate());
        entity.setPeriodEnd(domain.getPeriod().endDate());
        entity.setBaseAmount(domain.getBaseAmount().amount());
        entity.setDeductions(domain.getDeductions().amount());
        entity.setBonuses(domain.getBonuses().amount());
        entity.setTotalPaid(domain.getTotalPaid().amount());
        entity.setCurrency(domain.getCurrency().name());
        entity.setStatus(domain.getStatus());
        entity.setPaidAt(domain.getPaidAt());
        entity.setPaymentReference(domain.getPaymentReference());

        List<PayrollItemPersistenceEntity> itemEntities = new ArrayList<>();
        for (PayrollItem item : domain.getItems()) {
            PayrollItemPersistenceEntity itemEntity = new PayrollItemPersistenceEntity(item.getId().value());
            itemEntity.setPayrollPayment(entity);
            itemEntity.setCategory(item.getCategory());
            itemEntity.setConcept(item.getConcept());
            itemEntity.setAmount(item.getAmount().amount());
            itemEntity.setType(item.getType());
            itemEntity.setDate(item.getDate());
            itemEntities.add(itemEntity);
        }
        entity.setItems(itemEntities);

        return entity;
    }

    public void updateEntity(PayrollPaymentPersistenceEntity entity, PayrollPayment domain) {
        entity.setBaseAmount(domain.getBaseAmount().amount());
        entity.setDeductions(domain.getDeductions().amount());
        entity.setBonuses(domain.getBonuses().amount());
        entity.setTotalPaid(domain.getTotalPaid().amount());
        entity.setStatus(domain.getStatus());
        entity.setPaidAt(domain.getPaidAt());
        entity.setPaymentReference(domain.getPaymentReference());

        entity.getItems().clear();
        for (PayrollItem item : domain.getItems()) {
            PayrollItemPersistenceEntity itemEntity = new PayrollItemPersistenceEntity(item.getId().value());
            itemEntity.setPayrollPayment(entity);
            itemEntity.setCategory(item.getCategory());
            itemEntity.setConcept(item.getConcept());
            itemEntity.setAmount(item.getAmount().amount());
            itemEntity.setType(item.getType());
            itemEntity.setDate(item.getDate());
            entity.getItems().add(itemEntity);
        }
    }
}
