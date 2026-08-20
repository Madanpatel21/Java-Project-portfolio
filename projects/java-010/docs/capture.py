"""JAVA-010 live demo capture (run by scripts/capture.py)."""
import json
import urllib.request

LOAD_USER = "manager"

def call(base, method, path, token=None, body=None, idem=None):
    h = {}
    if token: h["Authorization"] = f"Bearer {token}"
    if idem: h["Idempotency-Key"] = idem
    data = json.dumps(body).encode() if body is not None else None
    if data is not None: h["Content-Type"] = "application/json"
    r = urllib.request.Request(f"{base}{path}", data=data, headers=h, method=method)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def token(base, u):
    code, body = call(base, "POST", "/api/v1/auth/token",
                      body={"username": u, "password": "Password123!"})
    return json.loads(body)["accessToken"]

def run(s, base):
    T_EMP, T_MGR, T_ADM, T_AUD = (
        token(base, u) for u in ["employee", "manager", "admin", "auditor"])
    s.log("JAVA-010 Capacity & Shift Rostering Optimizer — LIVE DEMO (real server output)")
    s.log("=============================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # manager inspects the workforce (5 seeded employees, OPS department)")
    code, body = call(base, "GET", "/api/v1/employees", T_MGR)
    for emp in json.loads(body):
        s.log(f"  -> {emp['empNo']} {emp['name']:14} skills={emp['skills']} "
              f"max={emp['maxWeeklyHours']}h")
    s.log("")
    s.log("$ # manager creates a 7-day roster from the demand curve (MORNING/AFTERNOON/NIGHT)")
    code, body = call(base, "POST", "/api/v1/rosters", T_MGR,
                      body={"name": "Week 35 OPS", "department": "OPS",
                            "startDate": "2026-08-24", "days": 7,
                            "demand": [
                                {"shiftType": "MORNING", "startHour": 6,
                                 "durationHours": 8, "requiredSkill": "NURSE",
                                 "headcount": 1},
                                {"shiftType": "AFTERNOON", "startHour": 14,
                                 "durationHours": 8, "requiredSkill": "CARE",
                                 "headcount": 1},
                                {"shiftType": "NIGHT", "startHour": 22,
                                 "durationHours": 8, "requiredSkill": "NURSE",
                                 "headcount": 1}]})
    roster = json.loads(body)
    s.log(f"  -> roster {roster['name']} {roster['startDate']}..{roster['endDate']} "
          f"status={roster['status']} shifts={roster['shiftCount']}")
    s.log("")
    s.log("$ # manager runs the Timefold optimizer (5s time limit)")
    code, body = call(base, "POST", f"/api/v1/rosters/{roster['id']}/optimize", T_MGR)
    d = json.loads(body)
    s.log(f"  -> score={d['score']} feasible={d['feasible']} "
          f"assigned={d['assigned']}/{d['totalShifts']}")
    breakdown = json.loads(d["breakdownJson"])
    for name, weight in sorted(breakdown["constraints"].items()):
        s.log(f"     constraint '{name}': weight {weight}")
    s.log("")
    s.log("$ # auditor explains the score: every constraint match, with counts")
    code, body = call(base, "GET", f"/api/v1/rosters/{roster['id']}/explain", T_AUD)
    d = json.loads(body)
    for match in d["matches"]:
        if match["count"] > 0:
            s.log(f"     {match['constraint']:26} matches={match['count']:2} "
                  f"hard={match['hard']} soft={match['soft']}")
    s.log("")
    s.log("$ # employee views their own published schedule (self-service)")
    code, body = call(base, "GET", f"/api/v1/my/shifts/{roster['id']}", T_EMP)
    my_shifts = json.loads(body)
    for shift in my_shifts[:4]:
        s.log(f"  -> {shift['shiftDate']} {shift['shiftType']:9} {shift['startHour']}:00 "
              f"skill={shift['requiredSkill']}")
    s.log("")
    s.log("$ # employee requests a swap: pick a NURSE shift, target a NURSE-qualified peer")
    code, body = call(base, "GET", "/api/v1/employees", T_MGR)
    employees = json.loads(body)
    target = None
    for emp in employees:
        if emp["empNo"] != "EMP-101" and "NURSE" in emp["skills"]:
            target = emp
            break
    swap_shift = next(sh for sh in my_shifts if sh["requiredSkill"] == "NURSE")
    code, body = call(base, "POST", "/api/v1/my/swaps", T_EMP,
                      body={"assignmentId": swap_shift["assignmentId"],
                            "targetEmployeeId": target["id"],
                            "reason": "family event"})
    swap = json.loads(body)
    s.log(f"  -> {swap['swapNo']} status={swap['status']} target={target['empNo']}")
    s.log("")
    s.log("$ # manager approves the swap (skill + availability re-validated)")
    code, body = call(base, "POST", f"/api/v1/swaps/{swap['id']}/decide", T_MGR,
                      body={"decision": "APPROVED", "note": "ok"})
    d = json.loads(body)
    s.log(f"  -> {d['swapNo']} status={d['status']} reviewedBy={d['reviewedBy']}")
    s.log("")
    s.log("$ # roster coverage + fairness analytics")
    code, body = call(base, "GET", f"/api/v1/my/stats/{roster['id']}", T_MGR)
    stats = json.loads(body)
    s.log(f"  -> coverage={stats['coveragePct']}% fairnessStdDev={stats['fairnessStdDevHours']}h "
          f"pendingSwaps={stats['pendingSwaps']}")
    for row in stats["perEmployee"]:
        s.log(f"     {row['empNo']} {row['name']:14} {row['hours']}h/week")
    s.log("")
    s.log("$ # manager publishes the roster (full coverage required)")
    code, body = call(base, "POST", f"/api/v1/rosters/{roster['id']}/publish", T_MGR)
    d = json.loads(body)
    s.log(f"  -> status={d['status']} assigned={d['assignedCount']}/{d['shiftCount']}")
    s.log("")
    s.log("DEMO COMPLETE — constraint-based rostering with Timefold, explainable scores,")
    s.log("self-service schedules and manager-approved shift swaps")
