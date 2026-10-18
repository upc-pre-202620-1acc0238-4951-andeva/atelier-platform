package com.andeva.atelier.platform.invoicing.application.internal.eventhandlers;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.CustomerFiscalValidationAclService;
import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import com.andeva.atelier.platform.invoicing.domain.model.events.VoucherAcceptedBySunatEvent;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * Delivers the electronic receipt (PDF and XML hosted by the PSE) to the customer's email
 * once the voucher acceptance by SUNAT has been committed.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class VoucherReceiptEmailEventHandler {

    private static final Logger log = LoggerFactory.getLogger(VoucherReceiptEmailEventHandler.class);

    private final ElectronicVoucherRepository voucherRepository;
    private final CustomerFiscalValidationAclService customerAclService;
    private final VoucherReceiptEmailGateway emailGateway;

    public VoucherReceiptEmailEventHandler(
            ElectronicVoucherRepository voucherRepository,
            CustomerFiscalValidationAclService customerAclService,
            VoucherReceiptEmailGateway emailGateway
    ) {
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "Voucher repository cannot be null");
        this.customerAclService = Objects.requireNonNull(customerAclService, "Customer ACL service cannot be null");
        this.emailGateway = Objects.requireNonNull(emailGateway, "Email gateway cannot be null");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(VoucherAcceptedBySunatEvent event) {
        voucherRepository.findById(event.voucherId()).ifPresentOrElse(voucher -> {
            String voucherNumber = voucher.getSerie().value() + "-" + voucher.getNumber().format();
            customerAclService.getCustomerEmail(voucher.getCustomerId().value()).ifPresentOrElse(
                    email -> emailGateway.sendVoucherReceiptEmail(
                            voucher.getId().value(),
                            email,
                            voucher.getCustomerFiscalInfo().legalName(),
                            voucherNumber,
                            event.urls().pdfUrl(),
                            event.urls().xmlUrl()
                    ),
                    () -> log.info("Voucher {} accepted but customer has no email registered; receipt email skipped", voucherNumber)
            );
        }, () -> log.warn("Voucher {} not found when dispatching receipt email", event.voucherId().value()));
    }
}
