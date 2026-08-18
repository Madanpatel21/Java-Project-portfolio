package com.java700.workforce.security;

/** Role constants used by RBAC and mirrored in the Keycloak realm + local user store. */
public final class Roles {

    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String ACCESS_MANAGER = "ACCESS_MANAGER";
    public static final String COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";
    public static final String COMPLIANCE_ADMIN = "COMPLIANCE_ADMIN";
    public static final String AUDITOR = "AUDITOR";
    public static final String INTEGRATION = "INTEGRATION";

    private Roles() {
    }
}
