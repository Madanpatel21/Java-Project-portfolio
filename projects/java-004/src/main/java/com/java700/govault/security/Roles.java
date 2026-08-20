package com.java700.govault.security;

/** RBAC roles + classification-clearance model for the document governance vault. */
public final class Roles {

    public static final String RECORDS_MANAGER = "RECORDS_MANAGER";
    public static final String LEGAL_COUNSEL = "LEGAL_COUNSEL";
    public static final String BUSINESS_OWNER = "BUSINESS_OWNER";
    public static final String AUDITOR = "AUDITOR";
    public static final String ADMIN = "ADMIN";

    private Roles() {
    }

    /** Classification clearance per role: PUBLIC..RESTRICTED map to levels 0..4. */
    public static int clearance(String role) {
        return switch (role) {
            case RECORDS_MANAGER, LEGAL_COUNSEL, ADMIN -> 4;
            case BUSINESS_OWNER -> 3;
            case AUDITOR -> 2;
            default -> 0;
        };
    }
}
