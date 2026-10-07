package com.andeva.atelier.platform.invoicing.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary DTO of an electronic voucher for workshop and fleet queries.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherSummaryDto(
        UUID voucherId,
        String fullVoucherNumber,
        String voucherType,
        BigDecimal totalAmount,
        String status,
        boolean isFullyPaid
) {}
