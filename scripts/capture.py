#!/usr/bin/env python3
"""
Live-capture helper for project README proof-of-work assets.

Usage:
  python3 scripts/capture.py <project_dir> <demo_module> [load_paths...]

- runs <project_dir>/docs/capture.py (a module with `run(session)` that performs the demo
  and logs terminal lines),
- runs a 400-request / 10-worker load test against the given API paths,
- writes docs/session-demo.txt and docs/session-perf.txt.
"""
import concurrent.futures
import json
import sys
import time
import urllib.error
import urllib.request


def call(base, method, path, token=None, body=None, idem=None):
    h = {}
    if token:
        h["Authorization"] = f"Bearer {token}"
    if idem:
        h["Idempotency-Key"] = idem
    data = json.dumps(body).encode() if body is not None else None
    if data is not None:
        h["Content-Type"] = "application/json"
    r = urllib.request.Request(f"{base}{path}", data=data, headers=h, method=method)
    try:
        with urllib.request.urlopen(r, timeout=15) as resp:
            return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


def token(base, user, password="Password123!"):
    code, body = call(base, "POST", "/api/v1/auth/token",
                      body={"username": user, "password": password})
    return json.loads(body)["accessToken"]


class Session:
    def __init__(self, path):
        self.f = open(path, "w")

    def log(self, line=""):
        self.f.write(line + "\n")

    def close(self):
        self.f.close()


def load_test(base, paths, token, out):
    def hit(i):
        path = paths[i % len(paths)]
        r = urllib.request.Request(f"{base}{path}",
                                   headers={"Authorization": f"Bearer {token}"})
        t0 = time.perf_counter()
        try:
            with urllib.request.urlopen(r, timeout=10) as resp:
                code = resp.status
        except urllib.error.HTTPError as e:
            code = e.code
        return code, time.perf_counter() - t0

    codes, times = {}, []
    t0 = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as ex:
        for code, dt in ex.map(hit, range(400)):
            codes[code] = codes.get(code, 0) + 1
            times.append(dt)
    total = time.perf_counter() - t0
    times.sort()
    n = len(times)

    def pct(p):
        return times[min(n - 1, int(n * p))] * 1000

    s = Session(out)
    s.log("LOAD TEST (real measurements against the live server)")
    s.log("====================================================")
    s.log("")
    s.log("$ # 400 mixed GET requests, 10 concurrent workers")
    s.log("")
    s.log(f"  requests:      {n}")
    s.log(f"  status codes:  {dict(sorted(codes.items()))}")
    s.log(f"  total time:    {total:.2f}s")
    s.log(f"  throughput:    {n / total:.1f} req/s")
    s.log(f"  latency p50:   {pct(0.50):.1f} ms")
    s.log(f"  latency p95:   {pct(0.95):.1f} ms")
    s.log(f"  latency p99:   {pct(0.99):.1f} ms")
    s.log(f"  max:           {times[-1] * 1000:.1f} ms")
    s.log("")
    s.log("$ curl -s /actuator/prometheus  (business metrics after the run)")
    try:
        data = urllib.request.urlopen(f"{base}/actuator/prometheus").read().decode()
        for line in data.splitlines():
            metric_name = line.split("{")[0].split(" ")[0]
            if not line.startswith("#") and metric_name in {
                    "contracts_total", "obligations_total", "obligations_open",
                    "obligations_completed_total", "obligations_waived_total",
                    "obligations_reminders_sent_total",
                    "p2p_invoices_ingested_total", "p2p_invoices_matched_total",
                    "p2p_exceptions_open", "p2p_exceptions_waived_total",
                    "p2p_postings_total",
                    "expfraud_claims_submitted_total", "expfraud_claims_scored_total",
                    "expfraud_cases_opened_total", "expfraud_cases_confirmed_fraud_total",
                    "expfraud_cases_cleared_total", "expfraud_tips_received_total",
                    "expfraud_duplicates_groups_created_total",
                    "expfraud_duplicates_groups_total",
                    "expfraud_claims_risk_score_count", "expfraud_claims_risk_score_sum",
                    "expfraud_scoring_duration_seconds_count",
                    "expfraud_scoring_duration_seconds_sum",
                    "fleet_tasks_forecasted_total", "fleet_tasks_overdue_total",
                    "fleet_workorders_completed_total", "fleet_workorders_parts_hold_total",
                    "fleet_parts_issued_total", "fleet_odometer_tamper_flags_total",
                    "fleet_inspections_failed_total",
                    "roster_rosters_created_total", "roster_rosters_optimized_total",
                    "roster_rosters_published_total", "roster_swaps_approved_total",
                    "roster_swaps_rejected_total",
                    "roster_optimization_duration_seconds_count",
                    "roster_optimization_duration_seconds_sum",
                    "stewardship_reviews_total", "stewardship_prescriptions_total",
                    "stewardship_interventions_proposed_total",
                    "stewardship_interventions_accepted_total",
                    "stewardship_alerts_drug_bug_mismatch_total",
                    "stewardship_evaluation_duration_seconds_count",
                    "stewardship_evaluation_duration_seconds_sum"}:
                s.log("  " + metric_name)
    except Exception as e:
        s.log(f"  (metrics fetch skipped: {e})")
    s.log("")
    s.log("LOAD TEST COMPLETE")
    s.close()


def main():
    args = [arg for arg in sys.argv[1:]]
    perf_only = "--perf-only" in args
    if perf_only:
        args.remove("--perf-only")
    project_dir = args[0]
    demo_module = args[1]
    paths = args[2:] or ["/api/v1/contracts", "/actuator/health"]
    import importlib.util

    base = "http://localhost:8080"
    demo_path = f"{project_dir}/docs/capture.py"
    spec = importlib.util.spec_from_file_location("demo", demo_path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    if not perf_only:
        session = Session(f"{project_dir}/docs/session-demo.txt")
        try:
            mod.run(session, base)
        finally:
            session.close()
    user = getattr(mod, "LOAD_USER", "auditor")
    t = token(base, user)
    load_test(base, paths, t, f"{project_dir}/docs/session-perf.txt")
    print("captured demo + perf sessions")


if __name__ == "__main__":
    main()
