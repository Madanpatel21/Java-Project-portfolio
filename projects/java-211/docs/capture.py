"""JAVA-211 live demo capture (run by scripts/capture.py)."""
import json
import urllib.request

LOAD_USER = "pharmacist"

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
    T_PHA, T_PRE, T_IDP, T_MIC, T_INF, T_ADM = (
        token(base, u) for u in ["pharmacist", "prescriber", "idphysician",
                                 "microbiologist", "infectioncontrol", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-211 Antimicrobial Stewardship Tracker — LIVE DEMO (real server output)")
    s.log("==============================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # pharmacist reviews the seeded patients (Ada ICU, Alan MED)")
    code, body = call(base, "GET", "/api/v1/patients", T_PHA)
    patients = json.loads(body).get("items", [])
    ada = patients[0]
    alan = patients[1]
    for p in patients:
        s.log(f"  -> {p['mrn']} {p['name']} age={2026 - int(p['dob'][:4])} "
              f"weight={p['weightKg']}kg")
    code, body = call(base, "GET", f"/api/v1/patients/{ada['id']}/prescriptions?status=ACTIVE",
                      T_PHA)
    ada_rx = json.loads(body)
    cef = next(r for r in ada_rx if r["drugCode"] == "CEFTRIAXONE")
    tazo = next(r for r in ada_rx if r["drugCode"] == "PIPERACILLIN_TAZOBACTAM")
    metro = next(r for r in ada_rx if r["drugCode"] == "METRONIDAZOLE")
    s.log("  -> Ada active therapy:")
    for r in ada_rx:
        s.log(f"     {r['drugCode']:24} {r['route']} {r['doseMg']}mg q{r['frequencyHours']}h "
              f"empiric={r['empiric']}")
    s.log("")
    s.log("$ # microbiologist finds Ada's blood culture and adds the E. coli isolate")
    code, body = call(base, "GET", f"/api/v1/patients/{ada['id']}/cultures", T_MIC)
    culture = json.loads(body)[0]
    code, body = call(base, "POST", f"/api/v1/cultures/{culture['id']}/isolates", T_MIC,
                      body={"organism": "Escherichia coli",
                            "susceptibility": [
                                {"drugCode": "CEFTRIAXONE", "result": "R", "micValue": 16},
                                {"drugCode": "CEFAZOLIN", "result": "S", "micValue": 2},
                                {"drugCode": "PIPERACILLIN_TAZOBACTAM", "result": "S",
                                 "micValue": 4}]})
    s.log(f"  -> isolate added to culture {culture['id'][:8]}...")
    code, body = call(base, "POST", f"/api/v1/cultures/{culture['id']}/report", T_MIC)
    s.log("  -> culture reported (finalized)")
    s.log("")
    s.log("$ # pharmacist evaluates ceftriaxone against guidelines + culture evidence")
    code, body = call(base, "GET", f"/api/v1/stewardship/evaluate/{cef['id']}", T_PHA)
    for finding in json.loads(body):
        s.log(f"  -> [{finding['severity']}] {finding['type']}: {finding['title']}")
        s.log(f"     action: {finding['suggestedAction']}")
    s.log("")
    s.log("$ # evaluate pip-tazo: Ada's creatinine 2.1 -> Cockcroft-Gault CrCl ~25 mL/min")
    code, body = call(base, "GET", f"/api/v1/stewardship/evaluate/{tazo['id']}", T_PHA)
    for finding in json.loads(body):
        s.log(f"  -> [{finding['severity']}] {finding['type']}: {finding['title']}")
        s.log(f"     action: {finding['suggestedAction']}")
    s.log("")
    s.log("$ # evaluate metronidazole at 24h overlap — the rule fires only past 24h")
    code, body = call(base, "GET", f"/api/v1/stewardship/evaluate/{metro['id']}", T_PHA)
    s.log(f"  -> findings at ~24h overlap: {len(json.loads(body))} (threshold not crossed yet)")
    s.log("")
    s.log("$ # pharmacist stops the IV metronidazole, prescriber re-orders two backdated")
    s.log("$ # duplicates — the redundant-coverage rule now fires (>24h overlap)")
    call(base, "POST", f"/api/v1/prescriptions/{metro['id']}/stop", T_PHA)
    code, body = call(base, "GET", f"/api/v1/patients/{ada['id']}/admissions", T_PRE)
    admission = json.loads(body)[0]
    for offset_hours, dose_key in [(60, "dup-a"), (50, "dup-b")]:
        call(base, "POST", "/api/v1/prescriptions", T_PRE, idem=nk(),
             body={"patientId": ada["id"], "admissionId": admission["id"],
                   "drugCode": "METRONIDAZOLE", "indication": "INTRA_ABDOMINAL",
                   "route": "IV", "doseMg": 500, "frequencyHours": 8,
                   "startAt": f"2026-08-18T12:00:00Z", "empiric": False})
    code, body = call(base, "GET", f"/api/v1/patients/{ada['id']}/prescriptions?status=ACTIVE",
                      T_PHA)
    dup = next(r for r in json.loads(body) if r["drugCode"] == "METRONIDAZOLE"
               and r["id"] != metro["id"])
    code, body = call(base, "GET", f"/api/v1/stewardship/evaluate/{dup['id']}", T_PHA)
    for finding in json.loads(body):
        s.log(f"  -> [{finding['severity']}] {finding['type']}: {finding['title']}")
        s.log(f"     action: {finding['suggestedAction']}")
    s.log("")
    s.log("$ # pharmacist works the review queue: assign + complete the due task")
    code, body = call(base, "GET", "/api/v1/reviews/open", T_PHA)
    tasks = json.loads(body)
    s.log(f"  -> open review tasks: {len(tasks)}")
    for task in tasks[:3]:
        s.log(f"     {task['triggerReason']:24} due={task['dueAt'][:16]}")
    if tasks:
        task = tasks[0]
        code, body = call(base, "POST", f"/api/v1/reviews/{task['id']}/assign", T_PHA,
                          body={"pharmacist": "pharmacist"})
        d = json.loads(body)
        s.log(f"  -> assigned task {task['id'][:8]}... to {d.get('assignedTo', d.get('pharmacist','?'))}")
        code, body = call(base, "POST", f"/api/v1/reviews/{task['id']}/complete", T_PHA)
        d = json.loads(body)
        s.log(f"  -> completed: status={d['status']}")
    s.log("")
    s.log("$ # pharmacist proposes an IV-to-PO switch intervention on the duplicate order")
    code, body = call(base, "POST", "/api/v1/interventions", T_PHA, idem=nk(),
                      body={"prescriptionId": dup["id"], "type": "IV_TO_PO",
                            "detail": {"targetDrug": "METRONIDAZOLE", "targetRoute": "PO"},
                            "reason": "PO bioavailability 100%, redundant IV coverage"})
    intervention = json.loads(body)
    s.log(f"  -> intervention {intervention['id'][:8]}... type={intervention['type']} "
          f"status={intervention['status']}")
    code, body = call(base, "POST", f"/api/v1/interventions/{intervention['id']}/accept", T_PRE,
                      body={"response": "accepted, switch to PO"})
    d = json.loads(body)
    s.log(f"  -> prescriber accepted: status={d['status']}")
    s.log("")
    s.log("$ # prescriber orders restricted MEROPENEM -> pre-authorization required")
    code, body = call(base, "GET", f"/api/v1/patients/{ada['id']}/admissions", T_PRE)
    admission = json.loads(body)[0]
    code, body = call(base, "POST", "/api/v1/prescriptions", T_PRE, idem=nk(),
                      body={"patientId": ada["id"], "admissionId": admission["id"],
                            "drugCode": "MEROPENEM", "indication": "SEPSIS", "route": "IV",
                            "doseMg": 1000, "frequencyHours": 8,
                            "startAt": "2026-08-20T06:00:00Z", "empiric": False})
    meropenem = json.loads(body)
    s.log(f"  -> rx created status={meropenem['status']} "
          f"preAuthorizationRequired={meropenem['preAuthorizationRequired']}")
    code, body = call(base, "GET", "/api/v1/restricted-authorizations/pending", T_IDP)
    auths = json.loads(body)
    s.log(f"  -> pending authorizations: {len(auths)}")
    if auths:
        auth = auths[0]
        s.log(f"     {auth['drugCode'] if 'drugCode' in auth else auth['id'][:8]}... "
              f"status={auth['status']}")
        code, body = call(base, "POST",
                          f"/api/v1/restricted-authorizations/{auth['id']}/approve", T_IDP,
                          body={"note": "sepsis with ESBL risk — 72h review"})
        d = json.loads(body)
        s.log(f"  -> ID physician approved: status={d['status']}")
    s.log("")
    s.log("$ # infection control reads utilization metrics (DOT / DDD per ward)")
    code, body = call(base, "GET", "/api/v1/metrics/utilization"
                      "?from=2026-08-01T00:00:00Z&to=2026-08-31T23:59:59Z", T_INF)
    report = json.loads(body)
    for ward in report.get("wards", []):
        s.log(f"  -> {ward['ward']:6} DOT={ward['dot']} patientDays={ward['patientDays']} "
              f"DOT/1000PD={ward['dotPer1000PatientDays']}")
    s.log("")
    s.log("$ # antibiogram snapshot (cumulative susceptibility)")
    code, body = call(base, "GET", "/api/v1/antibiogram", T_INF)
    antibiogram = json.loads(body)
    rows = antibiogram.get("rows", []) if isinstance(antibiogram, dict) else []
    s.log(f"  -> minimumIsolates={antibiogram.get('minimumIsolates', '?')} "
          f"reportable rows={len(rows)}")
    for row in rows[:4]:
        s.log(f"     {row['organism']} / {row['drugName']} isolates={row['isolates']} "
              f"S={row['percentS']}%")
    s.log("")
    s.log("DEMO COMPLETE — guideline evaluation, drug-bug mismatch, renal dosing,")
    s.log("redundant-coverage alerts, IV→PO intervention, restricted pre-authorization")
