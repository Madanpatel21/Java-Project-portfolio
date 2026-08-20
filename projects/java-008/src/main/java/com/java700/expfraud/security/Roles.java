package com.java700.expfraud.security;

/** RBAC roles for the expense fraud platform. */
public final class Roles {

    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String MANAGER = "MANAGER";
    public static final String FRAUD_INVESTIGATOR = "FRAUD_INVESTIGATOR";
    public static final String AUDITOR = "AUDITOR";
    public static final String ADMIN = "ADMIN";

    /** Roles allowed to open case records and see unmasked PII. */
    public static final String[] PRIVILEGED = {FRAUD_INVESTIGATOR, AUDITOR, ADMIN};

    private Roles() {
    }
}
