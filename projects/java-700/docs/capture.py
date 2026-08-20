"""JAVA-700 live demo capture (run by scripts/capture.py)."""
import json
import urllib.request

LOAD_USER = "supervisor"

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
    T_REG, T_REG2, T_SUP, T_STA, T_VER, T_ADM = (
        token(base, u) for u in ["registrar", "registrar2", "supervisor",
                                 "statistician", "verifier", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"
    s.log("JAVA-700 Digital ID & Civil Registry (CRVS) — LIVE DEMO (real server output)")
    s.log("===========================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # registrar captures a birth registration (four-eyes: capture != approve)")
    code, body = call(base, "POST", "/api/v1/registrations/birth", T_REG, idem=nk(),
                      body={"fullName": "Nadia Hussain", "dob": "1999-04-12", "sex": "F",
                            "placeOfBirth": "North District", "parentNames": "Amir & Laila"})
    birth = json.loads(body)
    s.log(f"  -> registration {birth['id'][:8]}... status={birth['status']}")
    s.log("")
    s.log("$ # the same registrar tries to approve her own capture -> SoD violation")
    code, body = call(base, "POST", f"/api/v1/registrations/{birth['id']}/approve",
                      T_REG, idem=nk(), body={"note": "self-approval attempt"})
    err = json.loads(body)
    s.log(f"  -> HTTP {code} {err.get('detail','')}")
    s.log("")
    s.log("$ # supervisor approves -> person record + checksummed national ID issued")
    code, body = call(base, "POST", f"/api/v1/registrations/{birth['id']}/approve",
                      T_SUP, idem=nk(), body={"note": "verified against hospital records"})
    d = json.loads(body)
    nadia_id = d["personId"]
    s.log(f"  -> registration status={d['status']} personId={nadia_id[:8]}...")
    code, body = call(base, "GET", f"/api/v1/persons/{nadia_id}", T_SUP)
    nadia = json.loads(body)
    s.log(f"  -> person {nadia['fullName']} nationalId={nadia['nationalId']} "
          f"status={nadia['status']}")
    s.log("")
    s.log("$ # registrar2 captures a second birth, then a near-duplicate (typo) birth")
    code, body = call(base, "POST", "/api/v1/registrations/birth", T_REG2, idem=nk(),
                      body={"fullName": "Priya Sharma", "dob": "2001-09-03", "sex": "F",
                            "placeOfBirth": "West District", "parentNames": "Rahul & Meera"})
    p1 = json.loads(body)
    p1d = json.loads(call(base, "POST", f"/api/v1/registrations/{p1['id']}/approve",
                          T_SUP, idem=nk(), body={})[1])
    priya_id = p1d["personId"]
    code, body = call(base, "POST", "/api/v1/registrations/birth", T_REG2, idem=nk(),
                      body={"fullName": "Priya Sharmaa", "dob": "2001-09-03", "sex": "F",
                            "placeOfBirth": "West District", "parentNames": "Rahul & Meera"})
    p2 = json.loads(body)
    call(base, "POST", f"/api/v1/registrations/{p2['id']}/approve", T_SUP, idem=nk(),
         body={})
    s.log("  -> two near-identical persons registered (fuzzy duplicate bait)")
    s.log("")
    s.log("$ # registrar registers a marriage (nadia is the spouse)")
    code, body = call(base, "POST", "/api/v1/registrations/marriage", T_REG, idem=nk(),
                      body={"personId": priya_id, "spousePersonId": nadia_id})
    marriage = json.loads(body)
    s.log(f"  -> marriage registration {marriage['id'][:8]}... status={marriage['status']}")
    code, body = call(base, "POST", f"/api/v1/registrations/{marriage['id']}/approve",
                      T_SUP, idem=nk(), body={})
    marriage_view = json.loads(body)
    nadia_national_id = marriage_view["payload"]["spouseNationalId"]
    s.log(f"  -> marriage approved; spouse record: {marriage_view['payload']['spouseName']} "
          f"nationalId={nadia_national_id}")
    s.log("")
    s.log("$ # admin reviews fuzzy duplicate candidates and confirms the match")
    code, body = call(base, "GET", "/api/v1/dedup/open", T_ADM)
    candidates = json.loads(body)
    if isinstance(candidates, dict):
        candidates = candidates.get("items", candidates.get("candidates", []))
    s.log(f"  -> open duplicate candidates: {len(candidates)}")
    for candidate in candidates[:3]:
        s.log(f"     {candidate['id'][:8]}... {candidate}")
    if candidates:
        code, body = call(base, "POST", f"/api/v1/dedup/{candidates[0]['id']}/confirm", T_ADM,
                          body={})
        d = json.loads(body)
        s.log(f"  -> confirmed: {d}")
    s.log("")
    s.log("$ # supervisor issues a birth certificate; verifier checks the token")
    code, body = call(base, "POST", "/api/v1/certificates", T_SUP, idem=nk(),
                      body={"personId": nadia_id, "type": "BIRTH"})
    cert = json.loads(body)
    s.log(f"  -> certificate {cert['id'][:8]}... token={cert['token'][:12]}...")
    code, body = call(base, "GET", f"/api/v1/certificates/verify/{cert['token']}", T_VER)
    d = json.loads(body)
    s.log(f"  -> external verification: valid={d.get('valid')} status={d.get('status')}")
    s.log("")
    s.log("$ # admin revokes the certificate; verification now fails")
    code, body = call(base, "POST", f"/api/v1/certificates/{cert['id']}/revoke", T_ADM,
                      body={"reason": "issued with typo, re-issued"})
    d = json.loads(body)
    s.log(f"  -> certificate status={d.get('status')}")
    code, body = call(base, "GET", f"/api/v1/certificates/verify/{cert['token']}", T_VER)
    d = json.loads(body)
    s.log(f"  -> external verification: valid={d.get('valid')} status={d.get('status')}")
    s.log("")
    s.log("$ # registrar registers a death; banks see DECEASED immediately after approval")
    code, body = call(base, "POST", "/api/v1/registrations/death", T_REG2, idem=nk(),
                      body={"personId": nadia_id, "cause": "natural causes"})
    death = json.loads(body)
    call(base, "POST", f"/api/v1/registrations/{death['id']}/approve", T_SUP, idem=nk(),
         body={})
    code, body = call(base, "GET", f"/api/v1/verify/person/{nadia_national_id}", T_VER)
    d = json.loads(body)
    s.log(f"  -> verifier query for {nadia_national_id}: exists={d.get('exists')} "
          f"status={d.get('status')}")
    s.log("")
    s.log("$ # statistician verifies the dual hash-chained ledgers")
    code, body = call(base, "GET", "/api/v1/ledger/verify", T_STA)
    d = json.loads(body)
    s.log(f"  -> global registry chain: {d}")
    code, body = call(base, "GET", f"/api/v1/ledger/person/{nadia_id}/verify", T_STA)
    d = json.loads(body)
    s.log(f"  -> per-person life chain: {d}")
    s.log("")
    s.log("$ # vital statistics (births / marriages / deaths per region)")
    code, body = call(base, "GET", "/api/v1/statistics/vital"
                      "?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z", T_STA)
    d = json.loads(body)
    for key, value in d.items():
        if isinstance(value, (int, float)):
            s.log(f"  -> {key}: {value}")
        elif isinstance(value, list):
            s.log(f"  -> {key}: {len(value)} row(s)")
    s.log("")
    s.log("DEMO COMPLETE — four-eyes life-event registration, fuzzy dedup, certificates,")
    s.log("deceased-status propagation and dual hash-chained ledgers")
