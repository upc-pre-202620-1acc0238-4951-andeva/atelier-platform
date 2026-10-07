package com.andeva.atelier.platform.invoicing.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result DTO containing metadata of an electronic voucher issued from a work order.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherGenerationResultDto(
        UUID voucherId,
        String fullVoucherNumber,
        BigDecimal totalAmount,
        String status,
        String pdfUrl
) {}
