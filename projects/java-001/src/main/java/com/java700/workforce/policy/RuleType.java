package com.java700.workforce.policy;

/** Typed policy rule families evaluated by the correlation engine. */
public enum RuleType {
    SOD_CONFLICT,
    CERT_EXPIRED,
    RECERT_OVERDUE,
    STANDING_PRIVILEGE,
    INACTIVE_ACCOUNT
}
