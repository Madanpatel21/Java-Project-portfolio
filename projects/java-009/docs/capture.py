"""JAVA-009 live demo capture (run by scripts/capture.py)."""
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
    T_DRV, T_FLT, T_MEC, T_CLK, T_COM, T_AUD, T_ADM = (
        token(base, u) for u in ["driver", "fleet", "mechanic", "clerk",
                                 "compliance", "auditor", "admin"])
    s.log("JAVA-009 Fleet Maintenance Planning System — LIVE DEMO (real server output)")
    s.log("========================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # fleet manager registers a truck: 98,500 km, last service at 90,000 km")
    code, body = call(base, "POST", "/api/v1/vehicles", T_FLT,
                      body={"vin": "VIN-DEMO-001", "plate": "DEMO-001", "make": "Volvo",
                            "model": "FH16", "modelYear": 2022, "category": "TRUCK",
                            "initialOdometer": 98500, "serviceAnchorOdometer": 90000,
                            "lastServiceDate": "2025-08-15", "department": "LOGISTICS",
                            "driverName": "Ana Dias"})
    vehicle = json.loads(body)
    s.log(f"  -> {vehicle['plate']} registered, odometer={vehicle['currentOdometer']}")
    s.log("")
    s.log("$ # driver submits a tampered (rolled-back) odometer reading")
    code, body = call(base, "POST", f"/api/v1/vehicles/{vehicle['id']}/odometer", T_DRV,
                      body={"reading": 98000, "source": "DRIVER"})
    err = json.loads(body)
    s.log(f"  -> HTTP {code} {err.get('detail','')}")
    s.log("")
    s.log("$ # driver submits a physically impossible jump (+20,000 km in a day)")
    code, body = call(base, "POST", f"/api/v1/vehicles/{vehicle['id']}/odometer", T_DRV,
                      body={"reading": 118500, "source": "DRIVER"})
    d = json.loads(body)
    s.log(f"  -> accepted={d['accepted']} flag={d['flag']}")
    s.log("")
    s.log("$ # fleet manager runs the due-service forecast (Quartz job also runs every 30 min)")
    code, body = call(base, "POST", "/api/v1/scheduling/forecast/run", T_FLT)
    d = json.loads(body)
    s.log(f"  -> created={d['created']} updated={d['updated']} overdue={d['overdue']}")
    code, body = call(base, "GET", "/api/v1/scheduling/tasks", T_FLT)
    for task in json.loads(body):
        s.log(f"     {task['taskNo']} {task['planCode']:14} {task['status']:8} "
              f"priority={task['priority']} due={task.get('dueOdometer', task.get('dueDate'))}")
    s.log("")
    s.log("$ # mechanic opens a work order for the OVERDUE oil change -> parts kit reserved")
    code, body = call(base, "GET", "/api/v1/scheduling/tasks", T_FLT)
    oil = next(t for t in json.loads(body) if t["planCode"] == "OIL-CHANGE")
    code, body = call(base, "POST", "/api/v1/work-orders", T_FLT,
                      body={"taskId": oil["id"]})
    wo = json.loads(body)
    s.log(f"  -> {wo['woNo']} status={wo['status']}")
    for res in wo["reservations"]:
        s.log(f"     reserved {res['partName']} x{res['quantity']} ({res['status']})")
    s.log("")
    s.log("$ # mechanic starts and completes the job (90.00 labor, odometer 118,500)")
    call(base, "POST", f"/api/v1/work-orders/{wo['id']}/start", T_MEC,
         body={"mechanic": "Bob Kumar"})
    code, body = call(base, "POST", f"/api/v1/work-orders/{wo['id']}/complete", T_MEC,
                      body={"mechanic": "Bob Kumar", "laborHours": 1.5, "laborCost": 90.00,
                            "odometerAtService": 118500, "note": "oil + filter replaced"})
    d = json.loads(body)
    s.log(f"  -> status={d['status']} partsCost={d['partsCost']} totalCost={d['totalCost']}")
    code, body = call(base, "GET", "/api/v1/inventory/parts", T_CLK)
    oil_part = next(p for p in json.loads(body) if p["partCode"] == "OIL-5W30")
    s.log(f"  -> OIL-5W30 onHand={oil_part['quantityOnHand']} reserved={oil_part['reservedQty']}")
    s.log("")
    s.log("$ # a second work order is opened for the DUE brake inspection")
    code, body = call(base, "GET", "/api/v1/scheduling/tasks", T_FLT)
    brake = next(t for t in json.loads(body) if t["planCode"] == "BRAKE-INSPECT")
    code, body = call(base, "POST", "/api/v1/work-orders", T_FLT,
                      body={"taskId": brake["id"]})
    wo2 = json.loads(body)
    s.log(f"  -> {wo2['woNo']} status={wo2['status']}")
    for res in wo2["reservations"]:
        s.log(f"     reserved {res['partName']} x{res['quantity']}")
    s.log("")
    s.log("$ # compliance officer records a FAILED DOT inspection -> COMPLIANCE_HOLD")
    code, body = call(base, "POST", f"/api/v1/inspections/{vehicle['id']}", T_COM,
                      body={"inspectionType": "DOT_ANNUAL", "inspector": "R. Shah",
                            "result": "FAIL", "notes": "brake imbalance detected"})
    d = json.loads(body)
    s.log(f"  -> {d['inspectionNo']} result={d['result']}")
    code, body = call(base, "GET", f"/api/v1/vehicles/{vehicle['id']}", T_FLT)
    s.log(f"  -> vehicle status={json.loads(body)['status']}")
    code, body = call(base, "GET", "/api/v1/inspections/compliance-report", T_COM)
    for row in json.loads(body):
        if row["planCode"] == "DOT-ANNUAL":
            s.log(f"  -> compliance report: {row['plate']} DOT-ANNUAL compliant={row['compliant']}")
    s.log("")
    s.log("$ # compliance officer re-inspects after repair: PASS -> hold released")
    code, body = call(base, "POST", f"/api/v1/inspections/{vehicle['id']}", T_COM,
                      body={"inspectionType": "DOT_ANNUAL", "inspector": "R. Shah",
                            "result": "PASS", "notes": "brakes replaced",
                            "validUntil": "2027-08-20"})
    d = json.loads(body)
    s.log(f"  -> {d['inspectionNo']} result={d['result']} validUntil={d['validUntil']}")
    code, body = call(base, "GET", f"/api/v1/vehicles/{vehicle['id']}", T_FLT)
    s.log(f"  -> vehicle status={json.loads(body)['status']}")
    s.log("")
    s.log("$ # auditor checks fleet-wide stats and cost-per-asset analytics")
    code, body = call(base, "GET", "/api/v1/stats", T_AUD)
    stats = json.loads(body)
    s.log(f"  -> activeVehicles={stats['activeVehicles']} due={stats['dueTasks']} "
          f"overdue={stats['overdueTasks']} openWO={stats['openWorkOrders']} "
          f"tamperFlags={stats['tamperFlags']}")
    for row in stats["costPerAsset"]:
        s.log(f"     {row['plate']}: {row['completedOrders']} orders, total {row['totalCost']}")
    s.log("")
    s.log("DEMO COMPLETE — meter/calendar scheduling, odometer tamper detection, work-order")
    s.log("lifecycle with parts kitting, compliance holds and cost analytics per asset")
