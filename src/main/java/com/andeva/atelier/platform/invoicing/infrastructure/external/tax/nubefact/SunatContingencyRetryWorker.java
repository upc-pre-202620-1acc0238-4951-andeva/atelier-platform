package com.andeva.atelier.platform.invoicing.infrastructure.external.tax.nubefact;

import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherStatus;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Asynchronous contingency worker that retries SUNAT/PSE dispatch for electronic vouchers
 * retained in {@link VoucherStatus#ISSUED} status due to temporary network or PSE outages,
 * without reallocating or duplicating fiscal correlatives.
 */
@Component
public class SunatContingencyRetryWorker {

    private static final Logger log = LoggerFactory.getLogger(SunatContingencyRetryWorker.class);

    private final ElectronicVoucherRepository voucherRepository;
    private final ElectronicVoucherCommandService voucherCommandService;

    public SunatContingencyRetryWorker(
            ElectronicVoucherRepository voucherRepository,
            ElectronicVoucherCommandService voucherCommandService
    ) {
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "ElectronicVoucherRepository cannot be null");
        this.voucherCommandService = Objects.requireNonNull(voucherCommandService, "ElectronicVoucherCommandService cannot be null");
    }

    @Scheduled(fixedDelayString = "${app.invoicing.sunat.retry-delay-ms:60000}")
    public int retryPendingIssuedVouchers() {
        List<ElectronicVoucher> pendingVouchers = voucherRepository.findAllByStatus(VoucherStatus.ISSUED);
        if (pendingVouchers == null || pendingVouchers.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (ElectronicVoucher voucher : pendingVouchers) {
            try {
                voucherCommandService.syncPendingSunatStatus(voucher.getId());
                processed++;
            } catch (Exception ex) {
                log.warn("Contingency retry failed for voucher {}-{}: {}",
                        voucher.getSerie().value(), voucher.getNumber().format(), ex.getMessage());
            }
        }
        return processed;
    }
}
