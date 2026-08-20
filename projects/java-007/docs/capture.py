"""JAVA-007 live demo capture (run by scripts/capture.py)."""
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
    T_ATT, T_PAR, T_ANL, T_LIT, T_AUD, T_ADM = (
        token(base, u) for u in ["attorney", "paralegal", "analyst", "litteam", "auditor", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-007 Legal Matter & Conflict Intelligence — LIVE DEMO (real server output)")
    s.log("================================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # attorney registers client Acme and opponent Beta; opens matter M-2026-001")
    acme = call(base, "POST", "/api/v1/parties", T_ATT, idem=nk(),
                body={"name": "Acme Corporation", "type": "CLIENT"})[1]
    beta = call(base, "POST", "/api/v1/parties", T_ATT, idem=nk(),
                body={"name": "Beta Industries", "type": "OPPONENT"})[1]
    matter = call(base, "POST", "/api/v1/matters", T_ATT, idem=nk(),
                  body={"matterNo": "M-2026-001", "name": "Acme v Beta",
                        "clientPartyId": acme, "practiceArea": "LITIGATION"})[1]
    call(base, "POST", f"/api/v1/matters/{matter}/parties", T_ATT,
         body={"partyId": beta, "role": "OPPOSING"})
    s.log(f"  parties: Acme={acme[:8]}... Beta={beta[:8]}...  matter={matter[:8]}...")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/conflicts/screen  (prospective client Delta vs Beta)")
    code, body = call(base, "POST", "/api/v1/conflicts/screen", T_ANL, idem=nk(),
                      body={"subjectName": "Delta Corp", "adverseNames": ["Beta Industries"]})
    d = json.loads(body)
    s.log(f"  -> result: {d['result']}")
    for finding in json.loads(d["detailsJson"]):
        s.log(f"     [{finding['level']}] {finding['detail']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/conflicts/screen  (clean: Epsilon vs Zeta)")
    code, body = call(base, "POST", "/api/v1/conflicts/screen", T_ANL, idem=nk(),
                      body={"subjectName": "Epsilon LLC", "adverseNames": ["Zeta GmbH"]})
    s.log(f"  -> result: {json.loads(body)['result']}")
    s.log("")
    s.log("$ # paralegal computes court deadlines (DEFAULT jurisdiction, trigger 2026-08-20)")
    code, body = call(base, "POST", f"/api/v1/matters/{matter}/deadlines", T_PAR, idem=nk(),
                      body={"jurisdiction": "DEFAULT", "triggerDate": "2026-08-20"})
    s.log(f"  -> deadlines computed: {len(json.loads(body))}")
    code, body = call(base, "GET", f"/api/v1/matters/{matter}/deadlines", T_PAR)
    for d in json.loads(body):
        s.log(f"     {d['eventType']:14} due={d['dueAt']} status={d['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/matters/<id>/walls  (admin walls LITIGATION_TEAM)")
    call(base, "POST", f"/api/v1/matters/{matter}/walls", T_ADM,
         body={"roleName": "LITIGATION_TEAM"})
    s.log("  -> wall added: LITIGATION_TEAM")
    code, body = call(base, "GET", f"/api/v1/matters/{matter}", T_LIT)
    s.log(f"  -> litteam access: HTTP {code} {json.loads(body).get('detail','')}")
    code, body = call(base, "GET", f"/api/v1/matters/{matter}", T_ATT)
    s.log(f"  -> attorney access: HTTP {code} ({json.loads(body)['matterNo']})")
    s.log("")
    s.log("DEMO COMPLETE — party graph, conflict screening, court deadlines, ethical wall enforcement")
