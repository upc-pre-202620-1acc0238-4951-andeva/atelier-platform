package com.andeva.atelier.platform.inventory.application.commandservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.PurchaseOrder;
import com.andeva.atelier.platform.inventory.domain.model.commands.AddPurchaseOrderItemCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CancelPurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.CreatePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.IssuePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.ReceivePurchaseOrderCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RemovePurchaseOrderItemCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface PurchaseOrderCommandService {

    Result<PurchaseOrder, ApplicationError> handle(CreatePurchaseOrderCommand command);

    Result<PurchaseOrder, ApplicationError> handle(AddPurchaseOrderItemCommand command);

    Result<PurchaseOrder, ApplicationError> handle(RemovePurchaseOrderItemCommand command);

    Result<PurchaseOrder, ApplicationError> handle(IssuePurchaseOrderCommand command);

    Result<PurchaseOrder, ApplicationError> handle(ReceivePurchaseOrderCommand command);

    Result<Void, ApplicationError> handle(CancelPurchaseOrderCommand command);
}
