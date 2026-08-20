"""JAVA-003 live demo capture (run by scripts/capture.py)."""
import datetime
import json
import urllib.request

LOAD_USER = "auditor"

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
    T_LEGAL, T_CM, T_FIN, T_AUD = (token(base, u) for u in ["legal", "cmanager", "finance", "auditor"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-003 Contract Lifecycle & Obligation Engine — LIVE DEMO (real server output)")
    s.log("================================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # tokens issued for: legal, cmanager, finance, auditor")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/contracts  (cmanager creates CT-1001, DRAFT)")
    code, body = call(base, "POST", "/api/v1/contracts", T_CM, {
        "contractNo": "CT-1001", "title": "Master Supply Agreement",
        "counterparty": "Acme Supplies Ltd",
        "effectiveFrom": "2026-09-01", "effectiveTo": "2028-08-31"}, idem=nk())
    ct = json.loads(body)
    s.log(f"  -> contract {ct['id'][:8]}... status={ct['status']}")
    CT_ID = ct["id"]
    s.log("")
    CONTENT = {"clauses": [
        {"number": "1.1", "title": "Term", "text": "12 months", "sensitivity": 1},
        {"number": "1.2", "title": "Price", "text": "100 USD per unit", "sensitivity": 3}]}
    s.log("$ curl -s -X POST /api/v1/contracts/<id>/versions  (clauses v1: price clause = sensitivity-3)")
    code, body = call(base, "POST", f"/api/v1/contracts/{CT_ID}/versions", T_CM,
                      {"contentJson": json.dumps(CONTENT)}, idem=nk())
    s.log(f"  -> version 1 created: HTTP {code}")
    s.log("")
    s.log("$ curl -s /api/v1/contracts/<id>/clauses  (as FINANCE, clearance 3 -> full text)")
    code, body = call(base, "GET", f"/api/v1/contracts/{CT_ID}/clauses", T_FIN)
    for line in json.dumps(json.loads(body), indent=2).splitlines():
        s.log(f"  {line}")
    s.log("")
    s.log("$ curl -s /api/v1/contracts/<id>/clauses  (as AUDITOR, clearance 2 -> REDACTED)")
    code, body = call(base, "GET", f"/api/v1/contracts/{CT_ID}/clauses", T_AUD)
    for line in json.dumps(json.loads(body), indent=2).splitlines():
        s.log(f"  {line}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/contracts/<id>/activate  (cmanager approves - 1 of 2)")
    code, body = call(base, "POST", f"/api/v1/contracts/{CT_ID}/activate", T_CM, {})
    s.log(f"  -> status: {json.loads(body)['status']}")
    s.log("$ curl -s -X POST /api/v1/contracts/<id>/activate  (legal approves - 2 of 2)")
    code, body = call(base, "POST", f"/api/v1/contracts/{CT_ID}/activate", T_LEGAL, {})
    s.log(f"  -> status: {json.loads(body)['status']}  (four-eyes complete)")
    s.log("")
    due = (datetime.datetime.utcnow() + datetime.timedelta(days=2)).strftime("%Y-%m-%dT%H:%M:%SZ")
    s.log("$ curl -s -X POST /api/v1/obligations  (payment due in 2 days, 30-day window, HIGH)")
    code, body = call(base, "POST", "/api/v1/obligations", T_CM, {
        "contractId": CT_ID, "type": "PAYMENT", "title": "Quarterly payment",
        "dueAt": due, "windowBeforeDays": 30, "criticality": "HIGH"}, idem=nk())
    ob = json.loads(body)
    s.log(f"  -> obligation {ob['id'][:8]}... status={ob['status']}")
    OB_ID = ob["id"]
    s.log("")
    s.log("$ curl -s -X POST /api/v1/obligations/scan  (SLA scan -> NOTIFIED)")
    code, body = call(base, "POST", "/api/v1/obligations/scan", T_CM)
    s.log(f"  {json.loads(body)}")
    s.log("")
    s.log("$ curl -s /api/v1/obligations?status=NOTIFIED")
    code, body = call(base, "GET", "/api/v1/obligations?status=NOTIFIED", T_CM)
    for o in json.loads(body)["items"]:
        s.log(f"  {o['id'][:8]} {o['type']:10} {o['status']:10} {o['title']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/obligations/<id>/waive  (as cmanager -> four-eyes blocks)")
    code, body = call(base, "POST", f"/api/v1/obligations/{OB_ID}/waive", T_CM, {}, idem=nk())
    s.log(f"  -> HTTP {code} {json.loads(body).get('detail','')}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/obligations/<id>/waive  (as legal -> WAIVED, audited)")
    code, body = call(base, "POST", f"/api/v1/obligations/{OB_ID}/waive", T_LEGAL,
                      {"note": "Payment terms renegotiated in amendment 2"}, idem=nk())
    d = json.loads(body)
    s.log(f"  -> status: {d['status']}  waived_by={d['waivedBy']}  reason='{d['waiverReason']}'")
    s.log("")
    s.log("DEMO COMPLETE — versioning, clearance redaction, four-eyes activation, SLA scan, waiver loop")
