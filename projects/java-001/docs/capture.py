"""JAVA-001 live demo capture (run by scripts/capture.py)."""
import json
import urllib.request

LOAD_USER = "grace"

def call(base, method, path, token=None, body=None, idem=None):
    h = {}
    if token: h["Authorization"] = f"Bearer {token}"
    if idem: h["Idempotency-Key"] = idem
    data = json.dumps(body).encode() if body is not None else None
    if data is not None: h["Content-Type"] = "application/json"
    r = urllib.request.Request(f"{base}{path}", data=data, headers=h, method=method)
    try:
        with urllib.request.urlopen(r, timeout=15) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def token(base, u):
    code, body = call(base, "POST", "/api/v1/auth/token",
                      body={"username": u, "password": "Password123!"})
    return json.loads(body)["accessToken"]

def run(s, base):
    T_ALICE, T_CAROL, T_DAVE, T_EVE, T_FRANK, T_GRACE, T_INT = (
        token(base, u) for u in ["alice", "carol", "dave", "eve", "frank", "grace",
                                 "integrator"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-001 Workforce Compliance Evidence Platform — LIVE DEMO (real server output)")
    s.log("===============================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # alice (EMPLOYEE) requests READ access to prod-orders-db for herself")
    code, body = call(base, "GET", "/api/v1/users?query=alice", T_CAROL)
    users = json.loads(body)
    items = users.get("items", users if isinstance(users, list) else [])
    subject = next(u for u in items if u["username"] == "alice")
    code, body = call(base, "POST", "/api/v1/access-requests", T_ALICE, idem=nk(),
                      body={"subjectUserId": subject["id"], "resourceType": "DATABASE",
                            "resourceName": "prod-orders-db", "roles": ["READ"],
                            "justification": "quarterly finance reconciliation"})
    request = json.loads(body)
    s.log(f"  -> request {request['id'][:8]}... status={request['status']} "
          f"subject={subject['username']} roles={request['roles']}")
    s.log("")
    s.log("$ # carol (ACCESS_MANAGER) approves — first of two required approvers")
    code, body = call(base, "POST", f"/api/v1/access-requests/{request['id']}/approve",
                      T_CAROL, idem=nk(), body={"comment": "verified with line manager"})
    d = json.loads(body)
    s.log(f"  -> status={d['status']}")
    s.log("")
    s.log("$ # carol tries again — dual control rejects duplicate approver")
    code, body = call(base, "POST", f"/api/v1/access-requests/{request['id']}/approve",
                      T_CAROL, idem=nk(), body={"comment": "double check"})
    err = json.loads(body)
    s.log(f"  -> HTTP {code} {err.get('detail','')}")
    s.log("")
    s.log("$ # dave (second ACCESS_MANAGER) approves — grant is created")
    code, body = call(base, "POST", f"/api/v1/access-requests/{request['id']}/approve",
                      T_DAVE, idem=nk(), body={"comment": "approved per policy ACCESS_GOVERNANCE"})
    d = json.loads(body)
    s.log(f"  -> request status={d['status']}")
    code, body = call(base, "GET", f"/api/v1/grants/{subject['id']}", T_GRACE)
    grants = json.loads(body).get("items", [])
    for grant in grants:
        s.log(f"  -> grant {grant['id'][:8]}... {grant['resourceName']} roles={grant['roles']} "
              f"status={grant['status']}")
    s.log("")
    s.log("$ # integrator ingests a raw access event from the HR system")
    code, body = call(base, "POST", "/api/v1/events/access", T_INT, idem=nk(),
                      body={"userId": subject["id"], "resourceName": "prod-orders-db",
                            "eventType": "LOGIN", "source": "hr-system"})
    d = json.loads(body)
    s.log(f"  -> eventId={d.get('eventId','?')[:12]}... duplicate={d.get('duplicate', False)}")
    s.log("")
    s.log("$ # frank (COMPLIANCE_ADMIN) publishes a new policy version (SoD rules)")
    code, body = call(base, "POST", "/api/v1/policies/ACCESS_GOVERNANCE/versions", T_FRANK,
                      body={"rulesJson": json.dumps(
                          [{"type": "SOD_CONFLICT", "severity": "HIGH",
                            "params": {"conflictPairs": [["APPROVER", "REQUESTER"],
                                                         ["ADMIN", "AUDITOR"]]}},
                           {"type": "RECERT_OVERDUE", "severity": "MEDIUM",
                            "params": {"intervalDays": 90}}])})
    d = json.loads(body)
    s.log(f"  -> policy ACCESS_GOVERNANCE new version {d.get('versionNo', '?')}")
    s.log("")
    s.log("$ # eve (COMPLIANCE_OFFICER) runs the correlation engine")
    code, body = call(base, "POST", "/api/v1/compliance/run", T_EVE)
    s.log(f"  -> HTTP {code}")
    code, body = call(base, "GET", "/api/v1/violations", T_EVE)
    violations = json.loads(body)
    if isinstance(violations, dict):
        violations = violations.get("items", violations)
    s.log(f"  -> violations: {len(violations)}")
    for violation in violations[:4]:
        s.log(f"     {violation['id'][:8]}... severity={violation.get('severity','?')} "
              f"status={violation['status']}")
    s.log("")
    s.log("$ # eve acknowledges -> remediates -> closes the first violation")
    if violations:
        vid = violations[0]["id"]
        code, body = call(base, "POST", f"/api/v1/violations/{vid}/acknowledge",
                          T_EVE, body={})
        code, body = call(base, "POST", f"/api/v1/violations/{vid}/remediate",
                          T_EVE, body={"note": "grant reviewed with line manager"})
        code, body = call(base, "POST", f"/api/v1/violations/{vid}/close",
                          T_EVE, body={})
        d = json.loads(body)
        s.log(f"  -> violation {vid[:8]}... now {d.get('status','?')}")
    s.log("")
    s.log("$ # frank generates a recertification campaign (30-day window)")
    code, body = call(base, "POST", "/api/v1/recertification/campaigns", T_FRANK,
                      body={"name": "Q3 Access Review", "windowDays": 30})
    campaign = json.loads(body)
    s.log(f"  -> campaign {campaign['id'][:8]}... window={campaign['windowStart'][:10]}.."
          f"{campaign['windowEnd'][:10]}")
    code, body = call(base, "GET", f"/api/v1/grants/{subject['id']}", T_GRACE)
    grants = json.loads(body).get("items", [])
    if grants:
        code, body = call(base, "POST", f"/api/v1/recertification/campaigns/{campaign['id']}/decisions",
                          T_FRANK, body={"grantId": grants[0]["id"], "decision": "KEEP"})
        d = json.loads(body)
        s.log(f"  -> decision recorded: {d.get('decision','?')}")
    s.log("")
    s.log("$ # grace (AUDITOR) verifies the evidence ledger hash chain")
    code, body = call(base, "GET", "/api/v1/evidence/verify", T_GRACE)
    d = json.loads(body)
    s.log(f"  -> {d}")
    code, body = call(base, "GET", f"/api/v1/evidence?aggregateType=ACCESS_REQUEST"
                                   f"&aggregateId={request['id']}", T_GRACE)
    evidence = json.loads(body)
    s.log(f"  -> ledger entries for the access request: {len(evidence)}")
    s.log("")
    s.log("$ # grace creates a signed auditor export bundle (JSONL + HMAC-SHA256)")
    code, body = call(base, "POST", "/api/v1/audit/exports", T_GRACE, idem=nk(),
                      body={"scopeUserId": subject["id"],
                            "rangeFrom": "2026-01-01T00:00:00Z",
                            "rangeTo": "2026-12-31T23:59:59Z"})
    export = json.loads(body)
    s.log(f"  -> job {export['id'][:8]}... status={export['status']}")
    code, body = call(base, "GET", f"/api/v1/audit/exports/{export['id']}/verify", T_GRACE)
    d = json.loads(body)
    s.log(f"  -> HMAC verification: {d}")
    code, body = call(base, "GET", f"/api/v1/audit/exports/{export['id']}/download", T_GRACE)
    s.log(f"  -> bundle downloaded: {len(body)} bytes of JSONL evidence")
    s.log("")
    s.log("DEMO COMPLETE — dual-control approvals, SoD enforcement, hash-chained evidence,")
    s.log("rule correlation, recertification and HMAC-signed auditor exports")
