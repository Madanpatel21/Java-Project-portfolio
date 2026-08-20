"""JAVA-006 live demo capture (run by scripts/capture.py)."""
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

CHAIN = """[{"step":1,"role":"MANAGER","approversRequired":2},
 {"step":2,"role":"DIRECTOR","approversRequired":1},
 {"step":3,"role":"LEGAL_COUNSEL","approversRequired":1}]"""

def run(s, base):
    T_REQ, T_M1, T_M2, T_DIR, T_LEGAL, T_AUD, T_ADM = (
        token(base, u) for u in ["requester", "manager", "manager2", "director", "legal", "auditor", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-006 Audit-Grade Approval & Policy Chain Engine — LIVE DEMO (real server output)")
    s.log("================================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/policies  (legal activates CAPEX_APPROVAL v1)")
    code, body = call(base, "POST", "/api/v1/policies", T_LEGAL, idem=nk(),
                      body={"policyCode": "CAPEX_APPROVAL", "name": "Capex Approval Policy",
                            "rulesJson": "{\"maxAmount\":100000}"})
    s.log(f"  -> policy created: {json.loads(body)['policyCode']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/chains  (admin creates a 3-step chain: MANAGER x2 -> DIRECTOR -> LEGAL)")
    code, body = call(base, "POST", "/api/v1/chains", T_ADM, idem=nk(),
                      body={"chainCode": "CAPEX_CHAIN", "name": "Capex Approval Chain",
                            "stepsJson": CHAIN})
    s.log(f"  -> chain created: {json.loads(body)['chainCode']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/requests  (requester submits PO-7001, bound to policy v1)")
    code, body = call(base, "POST", "/api/v1/requests", T_REQ, idem=nk(),
                      body={"chainCode": "CAPEX_CHAIN", "policyCode": "CAPEX_APPROVAL",
                            "subjectType": "CAPEX", "subjectId": "PO-7001",
                            "payload": {"amount": 75000},
                            "dueAt": "2026-08-27T00:00:00Z"})
    req = json.loads(body)
    s.log(f"  -> request {req['id'][:8]}... status={req['status']} step={req['currentStep']} policyV={req['policyVersionId'][:8]}")
    REQ_ID = req["id"]
    s.log("")
    s.log("$ curl -s -X POST /api/v1/requests/<id>/approve  (as requester -> SoD blocks)")
    code, body = call(base, "POST", f"/api/v1/requests/{REQ_ID}/approve", T_REQ, idem=nk(), body={})
    s.log(f"  -> HTTP {code}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/requests/<id>/approve  (manager approves - step 1 needs 2)")
    code, body = call(base, "POST", f"/api/v1/requests/{REQ_ID}/approve", T_M1, idem=nk(), body={})
    d = json.loads(body)
    s.log(f"  -> step={d['currentStep']} status={d['status']}")
    s.log("$ curl -s -X POST /api/v1/requests/<id>/approve  (manager2 completes dual control -> step 2)")
    code, body = call(base, "POST", f"/api/v1/requests/{REQ_ID}/approve", T_M2, idem=nk(), body={})
    d = json.loads(body)
    s.log(f"  -> step={d['currentStep']} status={d['status']}")
    s.log("$ curl -s -X POST /api/v1/requests/<id>/approve  (director -> step 3)")
    code, body = call(base, "POST", f"/api/v1/requests/{REQ_ID}/approve", T_DIR, idem=nk(), body={})
    d = json.loads(body)
    s.log(f"  -> step={d['currentStep']} status={d['status']}")
    s.log("$ curl -s -X POST /api/v1/requests/<id>/approve  (legal -> APPROVED)")
    code, body = call(base, "POST", f"/api/v1/requests/{REQ_ID}/approve", T_LEGAL, idem=nk(), body={})
    d = json.loads(body)
    s.log(f"  -> status={d['status']}")
    s.log("")
    s.log("$ curl -s /api/v1/requests/<id>/decisions  (audit-grade decision evidence)")
    code, body = call(base, "GET", f"/api/v1/requests/{REQ_ID}/decisions", T_AUD)
    for line in json.dumps(json.loads(body), indent=2).splitlines():
        s.log(f"  {line}")
    s.log("")
    s.log("DEMO COMPLETE — policy binding, 3-step chain, per-step dual control, SoD, evidence trail")
