package com.java700.contracts.security;

/** RBAC roles + clause-clearance model for the contract platform. */
public final class Roles {

    public static final String LEGAL_COUNSEL = "LEGAL_COUNSEL";
    public static final String CONTRACT_MANAGER = "CONTRACT_MANAGER";
    public static final String BUSINESS_OWNER = "BUSINESS_OWNER";
    public static final String FINANCE = "FINANCE";
    public static final String AUDITOR = "AUDITOR";
    public static final String ADMIN = "ADMIN";

    private Roles() {
    }

    /** Clause-clearance levels: readers see clause text only if their level >= clause sensitivity. */
    public static int clearance(String role) {
        return switch (role) {
            case LEGAL_COUNSEL, CONTRACT_MANAGER, ADMIN -> 4;
            case BUSINESS_OWNER, FINANCE -> 3;
            case AUDITOR -> 2;
            default -> 0;
        };
    }
}
