"""JAVA-008 live demo capture (run by scripts/capture.py)."""
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

def claim(base, tok, emp, name, dept, cat, amount, merchant, date, receipt, idem=None):
    return call(base, "POST", "/api/v1/claims", tok, idem=idem,
                body={"employeeId": emp, "employeeName": name, "department": dept,
                      "category": cat, "amount": amount, "currency": "USD",
                      "merchant": merchant, "expenseDate": date, "receiptRef": receipt,
                      "description": "business expense"})

def run(s, base):
    T_EMP, T_MGR, T_INV1, T_INV2, T_AUD, T_ADM = (
        token(base, u) for u in ["employee", "manager", "investigator",
                                 "investigator2", "auditor", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-008 Expense Fraud & Policy Analytics Engine — LIVE DEMO (real server output)")
    s.log("===============================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # employee submits a clean meal claim (44.25, receipt attached)")
    code, body = claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MEALS",
                       44.25, "Green Leaf", "2026-08-05", "r-601", idem=nk())
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']}  score={d['riskScore']} tier={d['riskTier']} status={d['status']}")
    s.log("")
    s.log("$ # manager approves it (low risk)")
    code, body = call(base, "POST", f"/api/v1/claims/{d['id']}/approve", T_MGR,
                      body={"note": "policy compliant"})
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']} status={d['status']}")
    s.log("")
    s.log("$ # employee submits weekend mileage: 245.75 km on a SATURDAY")
    code, body = claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MILEAGE",
                       245.75, "Shell Fuel", "2026-08-15", "r-602", idem=nk())
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']}  score={d['riskScore']} tier={d['riskTier']} status={d['status']}")
    for reason in d["reasons"]:
        s.log(f"     [{reason['severity']:8}] {reason['code']}: {reason['message']}")
    s.log("")
    s.log("$ # employee submits two near-identical claims at the same merchant (duplicate cluster)")
    claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MEALS",
          61.75, "Taj Kitchen", "2026-08-13", "r-603", idem=nk())
    code, body = claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MEALS",
                       61.75, "Taj Kitchen", "2026-08-14", "r-604", idem=nk())
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']}  score={d['riskScore']} tier={d['riskTier']} status={d['status']}")
    for reason in d["reasons"]:
        s.log(f"     [{reason['severity']:8}] {reason['code']}: {reason['message']}")
    s.log("")
    s.log("$ # investigator lists duplicate evidence groups")
    code, body = call(base, "GET", "/api/v1/claims/duplicate-groups", T_INV1)
    for group in json.loads(body):
        s.log(f"  -> group {group['id'][:8]}... merchant={group['merchant']} "
              f"size={group['size']} confidence={group['confidence']} claims={group['claimNos']}")
    s.log("")
    s.log("$ # employee submits two 350.00 mileage claims Fri+Sat at the same station -> HIGH risk")
    claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MILEAGE",
          350.00, "BP Fuel", "2026-08-14", "r-605", idem=nk())
    code, body = claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MILEAGE",
                       350.00, "BP Fuel", "2026-08-15", "r-606", idem=nk())
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']}  score={d['riskScore']} tier={d['riskTier']} status={d['status']}")
    for reason in d["reasons"]:
        s.log(f"     [{reason['severity']:8}] {reason['code']}: {reason['message']}")
    s.log("")
    s.log("$ # manager tries to approve the high-risk claim -> blocked by policy guard")
    code, body = call(base, "POST", f"/api/v1/claims/{d['id']}/approve", T_MGR,
                      body={"note": "looks fine to me"})
    err = json.loads(body)
    s.log(f"  -> HTTP {code} {err.get('title','')}: {err.get('detail','')}")
    s.log("")
    s.log("$ # investigator picks the case from the queue and recommends FRAUD")
    code, body = call(base, "GET", "/api/v1/cases", T_INV1)
    case = json.loads(body)[0]
    s.log(f"  -> {case['caseNo']}  score={case['riskScore']} status={case['status']}")
    code, body = call(base, "POST", f"/api/v1/cases/{case['id']}/review", T_INV1,
                      body={"recommendation": "RECOMMEND_FRAUD",
                            "note": "duplicate mileage with weekend pattern"})
    d = json.loads(body)
    s.log(f"  -> status={d['status']} reviewerOne={d['reviewerOne']}")
    s.log("")
    s.log("$ # same investigator tries to confirm -> four-eyes control rejects")
    code, body = call(base, "POST", f"/api/v1/cases/{case['id']}/decide", T_INV1,
                      body={"decision": "CONFIRM_FRAUD", "note": "I already reviewed this"})
    err = json.loads(body)
    s.log(f"  -> HTTP {code} {err.get('detail','')}")
    s.log("")
    s.log("$ # a second investigator confirms the fraud -> claim marked CONFIRMED_FRAUD")
    code, body = call(base, "POST", f"/api/v1/cases/{case['id']}/decide", T_INV2,
                      body={"decision": "CONFIRM_FRAUD", "note": "pattern confirmed"})
    d = json.loads(body)
    s.log(f"  -> case status={d['status']} decision={d['decision']} reviewerTwo={d['reviewerTwo']}")
    code, body = call(base, "GET", f"/api/v1/claims/{d['claimId']}", T_AUD)
    claim_view = json.loads(body)
    s.log(f"  -> claim {claim_view['claimNo']} status={claim_view['status']}")
    s.log("")
    s.log("$ # seed peer history, recompute baselines, then submit a 247.35 outlier meal")
    for i, amt in enumerate([42.55, 47.25, 51.05, 55.75, 62.30, 68.40]):
        claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MEALS",
              amt, f"Canteen {i}", f"2026-07-{10+i}", f"r-70{i}", idem=nk())
    code, body = call(base, "POST", "/api/v1/admin/baselines/recompute", T_ADM)
    s.log(f"  -> baselines recomputed: {len(json.loads(body))} buckets")
    code, body = call(base, "GET", "/api/v1/admin/baselines", T_ADM)
    for baseline in json.loads(body):
        if baseline["category"] == "MEALS":
            s.log(f"     {baseline['department']}/{baseline['category']} mean={baseline['mean']} "
                  f"stdDev={baseline['stdDev']} samples={baseline['sampleCount']}")
    code, body = claim(base, T_EMP, "emp-101", "Ravi Kumar", "ENGINEERING", "MEALS",
                       247.35, "Spice Garden", "2026-08-16", "r-710", idem=nk())
    d = json.loads(body)
    s.log(f"  -> {d['claimNo']}  score={d['riskScore']} tier={d['riskTier']} status={d['status']}")
    for reason in d["reasons"]:
        s.log(f"     [{reason['severity']:8}] {reason['code']}: {reason['message']}")
    s.log("")
    s.log("$ # anonymous whistleblower tip (no authentication required)")
    code, body = call(base, "POST", "/api/v1/tips",
                      body={"channel": "ANONYMOUS_WEB", "subject": "Fake mileage",
                            "description": "Employee inflates weekend mileage every month",
                            "relatedClaimNo": "EF-2026-00001"})
    tip = json.loads(body)
    s.log(f"  -> HTTP {code} {tip['tipNo']} status={tip['status']} (submitter identity not recorded)")
    code, body = call(base, "GET", "/api/v1/tips", T_INV1)
    s.log(f"  -> investigator sees {len(json.loads(body))} tip(s)")
    s.log("")
    s.log("$ # auditor checks platform stats and PII visibility (full name) vs employee (masked)")
    code, body = call(base, "GET", "/api/v1/admin/stats", T_AUD)
    stats = json.loads(body)
    s.log(f"  -> submitted={stats['claimsSubmitted']} underReview={stats['claimsUnderReview']} "
          f"confirmedFraud={stats['claimsConfirmedFraud']} avgRisk={stats['avgRiskScore']}")
    s.log("")
    s.log("DEMO COMPLETE — policy rules, weekend-mileage, duplicate clustering, peer outlier,")
    s.log("four-eyes case workflow, manager policy guard, anonymous whistleblower channel")
