-- Baseline compliance policies (version 1 of the ACCESS_GOVERNANCE policy).
-- rules_json is a JSON array of rule definitions:
--   { "type": RULE_TYPE, "severity": S|M|H, "params": { ... } }

INSERT INTO policy (id, code, name, description, active_version_id, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'ACCESS_GOVERNANCE',
        'Access Governance Baseline',
        'Segregation of duties, certification, recertification, standing privilege and account inactivity rules.',
        '00000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP);

INSERT INTO policy_version (id, policy_id, version_no, rules_json, status, effective_from, created_by, created_at)
VALUES ('00000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001',
        1,
        '[' ||
        ' {"type":"SOD_CONFLICT","severity":"HIGH","params":{"conflictPairs":[["APPROVER","REQUESTER"],["ADMIN","AUDITOR"]]}},' ||
        ' {"type":"CERT_EXPIRED","severity":"HIGH","params":{"certRequiredRoles":["ADMIN"]}},' ||
        ' {"type":"RECERT_OVERDUE","severity":"MEDIUM","params":{"intervalDays":90}},' ||
        ' {"type":"STANDING_PRIVILEGE","severity":"HIGH","params":{"privilegedRoles":["ADMIN"],"maxDays":90}},' ||
        ' {"type":"INACTIVE_ACCOUNT","severity":"MEDIUM","params":{"inactiveDays":60}}' ||
        ']',
        'ACTIVE', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
