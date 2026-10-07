package com.andeva.atelier.platform.invoicing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA Persistence Entity mapping itemized line items of an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
@Entity
@Table(name = "voucher_lines")
public class VoucherLinePersistenceEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private ElectronicVoucherPersistenceEntity voucher;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitValue;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "igv_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal igvAmount;

    @Column(name = "total_line", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLine;

    public VoucherLinePersistenceEntity() {
    }

    public VoucherLinePersistenceEntity(
            UUID id,
            ElectronicVoucherPersistenceEntity voucher,
            UUID itemId,
            String itemType,
            String description,
            BigDecimal quantity,
            BigDecimal unitValue,
            BigDecimal unitPrice,
            BigDecimal igvAmount,
            BigDecimal totalLine
    ) {
        this.id = id;
        this.voucher = voucher;
        this.itemId = itemId;
        this.itemType = itemType;
        this.description = description;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.unitPrice = unitPrice;
        this.igvAmount = igvAmount;
        this.totalLine = totalLine;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ElectronicVoucherPersistenceEntity getVoucher() {
        return voucher;
    }

    public void setVoucher(ElectronicVoucherPersistenceEntity voucher) {
        this.voucher = voucher;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(BigDecimal unitValue) {
        this.unitValue = unitValue;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getIgvAmount() {
        return igvAmount;
    }

    public void setIgvAmount(BigDecimal igvAmount) {
        this.igvAmount = igvAmount;
    }

    public BigDecimal getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(BigDecimal totalLine) {
        this.totalLine = totalLine;
    }
}
