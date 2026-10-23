package com.andeva.atelier.platform.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Configuration of the demo workshop, read from environment variables (see application.properties, section DEMO).
 * Credentials are never hard-coded.
 */
@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoSeedProperties {

    @Value("${app.demo.admin.email:}") private String adminEmail;
    @Value("${app.demo.admin.password:}") private String adminPassword;
    @Value("${DEMO_ADMIN_FIRST_NAME:Pedro}") private String adminFirstName;
    @Value("${DEMO_ADMIN_LAST_NAME:Suárez}") private String adminLastName;
    @Value("${app.demo.admin.phone:+51999111222}") private String adminPhone;
    @Value("${app.demo.mechanic.email:}") private String mechanicEmail;
    @Value("${app.demo.mechanic.password:}") private String mechanicPassword;
    @Value("${DEMO_MECHANIC_FIRST_NAME:Andrés}") private String mechanicFirstName;
    @Value("${DEMO_MECHANIC_LAST_NAME:Vílchez}") private String mechanicLastName;
    @Value("${app.demo.mechanic.phone:+51999333444}") private String mechanicPhone;
    @Value("${DEMO_WORKSHOP_NAME:Taller Suárez Automotriz}") private String workshopName;
    @Value("${DEMO_WORKSHOP_LEGAL_NAME:Suárez Servicios Automotrices S.A.C.}") private String workshopLegalName;
    @Value("${app.demo.workshop.ruc:20612345679}") private String workshopRuc;
    @Value("${app.demo.branch.name:Sede Principal}") private String branchName;
    @Value("${app.demo.branch.latitude:-12.0464}") private double branchLatitude;
    @Value("${app.demo.branch.longitude:-77.0428}") private double branchLongitude;
    @Value("${app.demo.branch.radius-meters:300}") private int branchRadiusMeters;
    @Value("${app.demo.plan-days:365}") private int planDays;

    public boolean isComplete() {
        return !adminEmail.isBlank() && !mechanicEmail.isBlank() && !mechanicPassword.isBlank();
    }

    /**
     * Plus-address of the owner's mailbox (e.g. owner+carlos@gmail.com), so emails sent to demo customers reach the presenter.
     */
    public String aliasOfAdmin(String tag) {
        int at = adminEmail.indexOf('@');
        String local = adminEmail.substring(0, at);
        int plus = local.indexOf('+');
        if (plus >= 0) {
            local = local.substring(0, plus);
        }
        return local + "+" + tag + adminEmail.substring(at);
    }

    public String adminEmail() { return adminEmail.trim(); }
    public String adminPassword() { return adminPassword; }
    public String adminFirstName() { return adminFirstName; }
    public String adminLastName() { return adminLastName; }
    public String adminPhone() { return adminPhone; }
    public String mechanicEmail() { return mechanicEmail.trim(); }
    public String mechanicPassword() { return mechanicPassword; }
    public String mechanicFirstName() { return mechanicFirstName; }
    public String mechanicLastName() { return mechanicLastName; }
    public String mechanicPhone() { return mechanicPhone; }
    public String workshopName() { return workshopName; }
    public String workshopLegalName() { return workshopLegalName; }
    public String workshopRuc() { return workshopRuc; }
    public String branchName() { return branchName; }
    public double branchLatitude() { return branchLatitude; }
    public double branchLongitude() { return branchLongitude; }
    public int branchRadiusMeters() { return branchRadiusMeters; }
    public int planDays() { return planDays; }
}
