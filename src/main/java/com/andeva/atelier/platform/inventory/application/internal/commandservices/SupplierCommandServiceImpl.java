package com.andeva.atelier.platform.inventory.application.internal.commandservices;

import com.andeva.atelier.platform.inventory.application.commandservices.SupplierCommandService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.DeactivateSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.repositories.SupplierRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class SupplierCommandServiceImpl implements SupplierCommandService {

    private final SupplierRepository supplierRepository;

    public SupplierCommandServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "supplierRepository cannot be null");
    }

    @Override
    public Result<Supplier, ApplicationError> handle(RegisterSupplierCommand command) {
        Objects.requireNonNull(command, "RegisterSupplierCommand cannot be null");

        if (supplierRepository.existsByTenantIdAndTaxId(command.tenantId(), command.taxId())) {
            return Result.failure(ApplicationError.conflict("Supplier with Tax ID '" + command.taxId().value() + "' already exists in this workshop"));
        }

        Supplier supplier = Supplier.create(
                command.tenantId(),
                command.businessName(),
                command.taxId(),
                command.contactName(),
                command.phone(),
                command.email(),
                command.address()
        );

        Supplier saved = supplierRepository.save(supplier);
        return Result.success(saved);
    }

    @Override
    public Result<Supplier, ApplicationError> handle(UpdateSupplierCommand command) {
        Objects.requireNonNull(command, "UpdateSupplierCommand cannot be null");

        Optional<Supplier> supplierOpt = supplierRepository.findById(command.supplierId());
        if (supplierOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Supplier", command.supplierId().value()));
        }

        Supplier supplier = supplierOpt.get();
        supplier.updateContactInfo(
                command.businessName(),
                command.contactName(),
                command.phone(),
                command.email(),
                command.address()
        );

        Supplier saved = supplierRepository.save(supplier);
        return Result.success(saved);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeactivateSupplierCommand command) {
        Objects.requireNonNull(command, "DeactivateSupplierCommand cannot be null");

        Optional<Supplier> supplierOpt = supplierRepository.findById(command.supplierId());
        if (supplierOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Supplier", command.supplierId().value()));
        }

        Supplier supplier = supplierOpt.get();
        supplier.deactivate();
        supplierRepository.save(supplier);
        return Result.success(null);
    }
}
