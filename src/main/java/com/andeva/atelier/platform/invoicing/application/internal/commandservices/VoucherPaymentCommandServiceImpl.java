package com.andeva.atelier.platform.invoicing.application.internal.commandservices;

import com.andeva.atelier.platform.invoicing.application.commandservices.VoucherPaymentCommandService;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.RegisterVoucherPaymentCommand;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.repositories.ElectronicVoucherRepository;
import com.andeva.atelier.platform.invoicing.domain.repositories.VoucherPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Transactional Command Service managing voucher payments, amortizations,
 * and balance settlement.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class VoucherPaymentCommandServiceImpl implements VoucherPaymentCommandService {

    private final ElectronicVoucherRepository voucherRepository;
    private final VoucherPaymentRepository paymentRepository;

    public VoucherPaymentCommandServiceImpl(
            ElectronicVoucherRepository voucherRepository,
            VoucherPaymentRepository paymentRepository
    ) {
        this.voucherRepository = Objects.requireNonNull(voucherRepository, "Voucher repository cannot be null");
        this.paymentRepository = Objects.requireNonNull(paymentRepository, "Payment repository cannot be null");
    }

    @Override
    public VoucherPayment handle(RegisterVoucherPaymentCommand command) {
        Objects.requireNonNull(command, "RegisterVoucherPaymentCommand cannot be null");

        ElectronicVoucher voucher = voucherRepository.findById(command.voucherId())
                .orElseThrow(() -> new VoucherNotFoundException(command.voucherId()));

        if (command.tenantId() != null && !voucher.getTenantId().equals(command.tenantId())) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_CROSS_TENANT_ACCESS",
                    "Voucher does not belong to the requesting tenant."
            );
        }
        if (command.branchId() != null && !voucher.getBranchId().equals(command.branchId())) {
            throw new com.andeva.atelier.platform.invoicing.domain.exceptions.InvoicingDomainException(
                    "ERR_CROSS_TENANT_ACCESS",
                    "Voucher does not belong to the requesting branch."
            );
        }

        VoucherPayment payment = voucher.recordPayment(
                PaymentId.generate(),
                command.amount(),
                command.method(),
                command.transactionReference()
        );

        voucherRepository.save(voucher);
        return paymentRepository.save(payment);
    }
}
