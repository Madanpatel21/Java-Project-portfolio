"""JAVA-004 live demo capture (run by scripts/capture.py)."""
import io
import json
import urllib.request

LOAD_USER = "auditor"

def call(base, method, path, token=None, body=None, idem=None, multipart=None):
    h = {}
    if token: h["Authorization"] = f"Bearer {token}"
    if idem: h["Idempotency-Key"] = idem
    if multipart is not None:
        boundary = "----java700boundary"
        h["Content-Type"] = f"multipart/form-data; boundary={boundary}"
        parts = []
        for name, filename, content in multipart:
            parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"; filename=\"{filename}\"\r\nContent-Type: text/plain\r\n\r\n".encode() + content + b"\r\n")
        parts.append(f"--{boundary}--\r\n".encode())
        data = b"".join(parts)
    else:
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
    T_REC, T_LEGAL, T_AUD, T_ADM = (token(base, u) for u in ["records", "legal", "auditor", "admin"])
    key = 0
    def nk():
        nonlocal key
        key += 1
        return f"demo-{key}"

    s.log("JAVA-004 Enterprise Document Governance Vault — LIVE DEMO (real server output)")
    s.log("================================================================================")
    s.log("")
    s.log("$ curl -s http://localhost:8080/actuator/health")
    code, body = call(base, "GET", "/actuator/health")
    s.log(f"  {json.loads(body)['status']}")
    s.log("")
    s.log("$ # upload contract.txt (multipart) -> enters QUARANTINED")
    code, body = call(base, "POST", "/api/v1/documents", T_REC, idem=nk(),
                      multipart=[("file", "contract.txt", b"Confidential pricing terms for the Acme deal\nPayment schedule Q4\n")])
    doc = json.loads(body)
    s.log(f"  -> document {doc['id'][:8]}... status={doc['status']} sha256={doc['contentHash'][:16]}...")
    DOC_ID = doc["id"]
    s.log("")
    s.log("$ curl -s -X POST /api/v1/documents/<id>/classify  (records manager: CONFIDENTIAL/R0)")
    code, body = call(base, "POST", f"/api/v1/documents/{DOC_ID}/classify", T_REC, idem=nk(),
                      body={"classification": "CONFIDENTIAL", "retentionClass": "R0"})
    d = json.loads(body)
    s.log(f"  -> status={d['status']} classification={d['classification']} retention={d['retentionClass']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/holds  (legal creates a litigation hold)")
    code, body = call(base, "POST", "/api/v1/holds", T_LEGAL, idem=nk(),
                      body={"name": "Litigation 2026-Q3", "reason": "Pending discovery in Acme v. Corp"})
    hold = json.loads(body)
    s.log(f"  -> hold {hold['id'][:8]}... status={hold['status']}")
    HOLD_ID = hold["id"]
    s.log("")
    s.log("$ curl -s -X POST /api/v1/holds/<hold>/apply/<doc>  (document under hold)")
    code, body = call(base, "POST", f"/api/v1/holds/{HOLD_ID}/apply/{DOC_ID}", T_LEGAL)
    d = json.loads(body)
    s.log(f"  -> hold status={d['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/retention/scan  (hold protects the document)")
    code, body = call(base, "POST", "/api/v1/retention/scan", T_REC)
    s.log(f"  {json.loads(body)}")
    s.log("")
    s.log("$ curl -s /api/v1/documents/search?q=Acme  (full-text over extracted content)")
    code, body = call(base, "GET", "/api/v1/documents/search?q=Acme", T_REC)
    for item in json.loads(body)["items"]:
        s.log(f"  {item['id'][:8]} {item['title']:18} {item['status']:12} hold={item['legalHold']}")
    s.log("")
    s.log("$ curl -s /api/v1/documents/<id>/download  (as auditor -> clearance blocks RESTRICTED/CONFIDENTIAL)")
    code, body = call(base, "GET", f"/api/v1/documents/{DOC_ID}/download", T_AUD)
    s.log(f"  -> HTTP {code} {json.loads(body).get('detail','')}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/holds/<hold>/release  (legal releases the hold)")
    code, body = call(base, "POST", f"/api/v1/holds/{HOLD_ID}/release", T_LEGAL)
    s.log(f"  -> hold status={json.loads(body)['status']}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/dev/documents/<id>/elapse-days/45  (DEV-ONLY time travel: age the doc past R0=30d)")
    code, body = call(base, "POST", f"/api/v1/dev/documents/{DOC_ID}/elapse-days/45", T_ADM)
    s.log(f"  -> HTTP {code}")
    s.log("")
    s.log("$ curl -s -X POST /api/v1/retention/scan  (past-retention doc now disposes with proof)")
    code, body = call(base, "POST", "/api/v1/retention/scan", T_REC)
    s.log(f"  {json.loads(body)}")
    s.log("")
    s.log("$ curl -s /api/v1/documents/<id>/disposition-proofs  (append-only proof of destruction)")
    code, body = call(base, "GET", f"/api/v1/documents/{DOC_ID}/disposition-proofs", T_AUD)
    for line in json.dumps(json.loads(body), indent=2).splitlines():
        s.log(f"  {line}")
    s.log("")
    s.log("DEMO COMPLETE — quarantine, classify, hold protection, clearance, disposition proof loop")
