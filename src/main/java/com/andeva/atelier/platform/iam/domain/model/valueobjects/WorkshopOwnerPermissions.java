package com.andeva.atelier.platform.iam.domain.model.valueobjects;

import java.util.Set;

/**
 * Canonical catalog of fine-grained authorities checked by the REST endpoints. The workshop
 * owner is the sovereign role of a tenant, so it is granted every one of them.
 * <p>
 * Interim measure until the permissions catalog is seeded and each role template receives its
 * own subset.
 */
public final class WorkshopOwnerPermissions {

    public static final Set<String> ALL = Set.of(
            "billing:invoices:read",
            "billing:subscriptions:manage_stripe",
            "billing:subscriptions:read",
            "crm:appointments:create",
            "crm:appointments:manage",
            "crm:appointments:read",
            "crm:appointments:update",
            "crm:customers:create",
            "crm:customers:manage",
            "crm:customers:read",
            "crm:customers:update",
            "crm:fleets:manage",
            "crm:vehicles:create",
            "crm:vehicles:read",
            "crm:vehicles:update",
            "hr:attendance:audit_all",
            "hr:attendance:clock_in",
            "hr:attendance:clock_out",
            "hr:attendance:read_own",
            "hr:justifications:approve",
            "hr:plame:export",
            "hr:shifts:manage",
            "hr:shifts:read",
            "iam:branches:manage",
            "iam:branches:read",
            "iam:members:compensate",
            "iam:members:invite",
            "iam:members:manage_roles",
            "iam:members:read",
            "iam:permissions:read",
            "iam:roles:create",
            "iam:roles:read",
            "iam:tenants:read",
            "iam:tenants:update",
            "inventory:alerts:evaluate",
            "inventory:alerts:manage",
            "inventory:alerts:read",
            "inventory:alerts:resolve",
            "inventory:batches:dispatch_fifo",
            "inventory:batches:manage",
            "inventory:batches:read",
            "inventory:batches:receive",
            "inventory:batches:restore",
            "inventory:parts:manage",
            "inventory:parts:read",
            "inventory:purchase_orders:cancel",
            "inventory:purchase_orders:create",
            "inventory:purchase_orders:manage",
            "inventory:purchase_orders:read",
            "inventory:purchase_orders:receive",
            "inventory:suppliers:manage",
            "inventory:suppliers:read",
            "invoicing:cashflow:export_pdf",
            "invoicing:fiscal_config:manage",
            "invoicing:invoices:issue_sunat",
            "invoicing:invoices:read",
            "invoicing:payments:create",
            "iot:alerts:acknowledge",
            "iot:alerts:convert",
            "iot:alerts:read",
            "iot:devices:manage",
            "iot:devices:read",
            "iot:devices:register",
            "iot:faults:read",
            "iot:faults:resolve",
            "iot:faults:write",
            "iot:health_reports:generate",
            "iot:health_reports:read",
            "iot:installations:manage",
            "iot:installations:read",
            "iot:telemetry:ingest",
            "iot:telemetry:read",
            "operations:bays:manage",
            "operations:bays:read",
            "operations:bays:write",
            "operations:services:read",
            "operations:services:write",
            "operations:tasks:complete",
            "operations:tasks:read",
            "operations:tasks:track_time",
            "operations:tasks:update",
            "operations:tasks:upload_photos",
            "operations:tasks:write",
            "operations:work-orders:read",
            "operations:work-orders:write"
    );

    private WorkshopOwnerPermissions() {
    }
}
