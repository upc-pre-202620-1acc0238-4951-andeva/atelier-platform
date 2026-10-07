package com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers;

import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CustomerFiscalInfo;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.SunatResponse;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.CustomerFiscalInfoResponse;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.DigitalReceiptUrlsResponse;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.ElectronicVoucherResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.SunatResponseDto;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.VoucherLineResource;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses.VoucherPaymentResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource Assembler transforming {@link ElectronicVoucher} domain aggregates into RESTful {@link ElectronicVoucherResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ElectronicVoucherResourceAssembler {

    public ElectronicVoucherResource toResource(ElectronicVoucher voucher) {
        if (voucher == null) {
            return null;
        }

        CustomerFiscalInfo fiscal = voucher.getCustomerFiscalInfo();
        CustomerFiscalInfoResponse customerResponse = new CustomerFiscalInfoResponse(
                fiscal != null && fiscal.taxId() != null ? fiscal.taxId().value() : "-",
                fiscal != null ? fiscal.legalName() : "",
                fiscal != null ? fiscal.fiscalAddress() : "",
                fiscal != null && fiscal.documentType() != null ? fiscal.documentType().name() : "OTROS"
        );

        DigitalReceiptUrls urls = voucher.getDigitalReceiptUrls();
        DigitalReceiptUrlsResponse receiptsResponse = new DigitalReceiptUrlsResponse(
                urls != null ? urls.pdfUrl() : "",
                urls != null ? urls.xmlUrl() : "",
                urls != null ? urls.cdrUrl() : ""
        );

        SunatResponseDto sunatResponse = null;
        if (voucher.getSunatResponse().isPresent()) {
            SunatResponse sr = voucher.getSunatResponse().get();
            sunatResponse = new SunatResponseDto(
                    sr.responseCode(),
                    sr.description(),
                    sr.digitalSignatureHash()
            );
        }

        List<VoucherLineResource> lineResources = new ArrayList<>();
        if (voucher.getLines() != null) {
            for (VoucherLine line : voucher.getLines()) {
                lineResources.add(toLineResource(line));
            }
        }

        List<VoucherPaymentResource> paymentResources = new ArrayList<>();
        if (voucher.getPayments() != null) {
            for (VoucherPayment payment : voucher.getPayments()) {
                paymentResources.add(toPaymentResource(payment));
            }
        }

        String typeCode = toSunatTypeCode(voucher.getVoucherType());

        return new ElectronicVoucherResource(
                voucher.getId().value(),
                voucher.getTenantId().value(),
                voucher.getBranchId().value(),
                voucher.getCustomerId().value(),
                voucher.getWorkOrderId().map(WorkOrderId::value).orElse(null),
                typeCode,
                voucher.getSerie().value(),
                voucher.getNumber().value(),
                voucher.getTaxCalculation().subtotal().amount(),
                voucher.getTaxCalculation().igvAmount().amount(),
                voucher.getTaxCalculation().totalAmount().amount(),
                voucher.getCurrency().name(),
                voucher.getStatus().name(),
                customerResponse,
                receiptsResponse,
                sunatResponse,
                lineResources,
                paymentResources,
                voucher.getIssuedAt()
        );
    }

    public VoucherLineResource toLineResource(VoucherLine line) {
        if (line == null) {
            return null;
        }
        return new VoucherLineResource(
                line.getId().value(),
                line.getItemId().orElse(null),
                line.getItemType().name(),
                line.getDescription(),
                line.getQuantity().value(),
                line.getUnitValue().amount(),
                line.getUnitPrice().amount(),
                line.getIgvAmount().amount(),
                line.getTotalLine().amount()
        );
    }

    public VoucherPaymentResource toPaymentResource(VoucherPayment payment) {
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

    public List<ElectronicVoucherResource> toResourceList(List<ElectronicVoucher> vouchers) {
        if (vouchers == null) {
            return List.of();
        }
        return vouchers.stream().map(this::toResource).toList();
    }

    private String toSunatTypeCode(VoucherType type) {
        if (type == null) return "01";
        return switch (type) {
            case FACTURA -> "01";
            case BOLETA -> "03";
            case NOTA_CREDITO -> "07";
        };
    }
}
