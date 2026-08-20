"""JAVA-005 live demo capture (run by scripts/capture.py)."""
import json
import urllib.request

LOAD_USER = "viewer"

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

DEF = """{"nodes":[
  {"id":"start","type":"START","next":"review"},
  {"id":"review","type":"APPROVAL","role":"APPROVER","slaHours":24,"next":"gw1","compensation":"comp1"},
  {"id":"gw1","type":"GATEWAY","conditions":[
     {"expr":"var.amount > 1000","next":"legal"},
     {"default":true,"next":"end"}]},
  {"id":"legal","type":"APPROVAL","role":"LEGAL","slaHours":48,"next":"end"},
  {"id":"comp1","type":"COMPENSATION","role":"COMPENSATION","slaHours":24,"next":"end"},
  {"id":"end","type":"END"}
]}"""

def run(s, base):
    T_ADM, T_OP, T_AP = (token(base, u) for u in ["padmin", "operator", "approver"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-005 Dynamic Workflow Orchestration Platform — LIVE DEMO (real server output)")
    s.log("================================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/definitions  (padmin creates EXPENSE_APPROVAL v1)")
    code, body = call(base, "POST", "/api/v1/definitions", T_ADM, idem=nk(),
                      body={"definitionKey": "EXPENSE_APPROVAL", "name": "Expense Approval",
                            "definitionJson": DEF})
    d = json.loads(body)
    s.log(f"  -> definition {d['id'][:8]}... v{d['versionNo']} status={d['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/instances  (operator starts EXP-9001, amount=5000)")
    code, body = call(base, "POST", "/api/v1/instances", T_OP, idem=nk(),
                      body={"definitionKey": "EXPENSE_APPROVAL", "businessKey": "EXP-9001",
                            "variables": {"amount": 5000}})
    inst = json.loads(body)
    s.log(f"  -> instance {inst['id'][:8]}... status={inst['status']} node={inst['currentNodeId']}")
    INST_ID = inst["id"]
    s.log("")
    s.log("$ curl -s /api/v1/tasks?role=APPROVER  (worklist: first approval task)")
    code, body = call(base, "GET", "/api/v1/tasks?role=APPROVER", T_AP)
    wl = json.loads(body)
    for t in wl["items"]:
        s.log(f"  {t['id'][:8]} {t['taskType']:10} role={t['assigneeRole']} status={t['status']}")
        TASK1 = t["id"]
    s.log("")
    s.log(f"$ curl -s -X POST /api/v1/tasks/<id>/complete  (approver approves)")
    code, body = call(base, "POST", f"/api/v1/tasks/{TASK1}/complete", T_AP, idem=nk(),
                      body={"result": {"approved": True}})
    d = json.loads(body)
    s.log(f"  -> instance status={d['status']} node={d['currentNodeId']}  (gateway routed amount>1000 -> legal)")
    s.log("")
    s.log("$ curl -s /api/v1/tasks?role=LEGAL  (legal review task appears)")
    code, body = call(base, "GET", "/api/v1/tasks?role=LEGAL", T_AP)
    for t in json.loads(body)["items"]:
        s.log(f"  {t['id'][:8]} {t['taskType']:10} role={t['assigneeRole']} status={t['status']}")
        TASK2 = t["id"]
    s.log("")
    s.log("$ curl -s -X POST /api/v1/tasks/<id>/complete  (legal approves -> COMPLETED)")
    code, body = call(base, "POST", f"/api/v1/tasks/{TASK2}/complete", T_AP, idem=nk(),
                      body={"result": {"approved": True}})
    d = json.loads(body)
    s.log(f"  -> instance status={d['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/instances  (second instance EXP-9002, amount=100)")
    code, body = call(base, "POST", "/api/v1/instances", T_OP, idem=nk(),
                      body={"definitionKey": "EXPENSE_APPROVAL", "businessKey": "EXP-9002",
                            "variables": {"amount": 100}})
    inst2 = json.loads(body)
    s.log(f"  -> instance {inst2['id'][:8]}... status={inst2['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/instances/<id>/cancel  (cancel -> compensation task created)")
    code, body = call(base, "POST", f"/api/v1/instances/{inst2['id']}/cancel", T_OP)
    d = json.loads(body)
    s.log(f"  -> instance status={d['status']}")
    s.log("")
    s.log("$ curl -s /api/v1/tasks?role=COMPENSATION  (compensation worklist)")
    code, body = call(base, "GET", "/api/v1/tasks?role=COMPENSATION", T_AP)
    for t in json.loads(body)["items"]:
        s.log(f"  {t['id'][:8]} {t['taskType']:14} role={t['assigneeRole']} status={t['status']}")
    s.log("")
    s.log("DEMO COMPLETE — versioned definition, gateway routing, dual-approval chain, cancel + compensation")
