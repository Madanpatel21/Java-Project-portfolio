# -*- coding: utf-8 -*-
"""Generates the master index README + per-category catalog markdown from data/catalog.json."""
import json, os
from collections import Counter

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
CATALOG_DIR = os.path.join(ROOT, "catalog")
os.makedirs(CATALOG_DIR, exist_ok=True)

with open(os.path.join(ROOT, "data", "catalog.json"), encoding="utf-8") as f:
    catalog = json.load(f)

CATEGORIES = ["Enterprise Business Platforms", "Banking / FinTech / Insurance",
              "Healthcare / Pharma / Life Sciences", "Manufacturing / Industrial IoT / Robotics",
              "Telecom / Networking / Media", "Cybersecurity / Identity / Secrets",
              "Logistics / Supply Chain / Fleet", "Energy / Utilities / Grid",
              "Automotive / Aerospace / Transportation", "Data / AI Infrastructure",
              "Developer / Platform Infrastructure", "Government / Compliance / Public Infrastructure"]

cat_counts = Counter(r["category"] for r in catalog)
tier_counts = Counter(r["difficulty"] for r in catalog)

# ---------------- MASTER INDEX ----------------
lines = []
lines.append("# JAVA-700 — Master Catalog: 700 Unique Industrial-Grade Java Projects")
lines.append("")
lines.append("A portfolio of **exactly 700** production-oriented Java (Spring Boot, Java 21+) system designs, "
             "spanning 60+ industry subdomains, graded from **Advanced** through **Expert**, **Architect**, "
             "**Enterprise Platform** to **Omega** engineering. Every entry is designed as a real industrial "
             "system — not a tutorial CRUD app — with production security, data architecture, observability, "
             "failure handling and local-first deployment requirements.")
lines.append("")
lines.append("## How to use this catalog")
lines.append("")
lines.append("1. Pick a **Project ID** (e.g. `JAVA-347`).")
lines.append("2. Ask for full production implementation of that ID.")
lines.append("3. Each implementation includes: complete Maven/Gradle project, package structure, migrations "
             "(Flyway), security config, tests (unit/integration/security/resilience), Docker + docker-compose, "
             "local observability stack, seed data, API docs, threat model, runbook and ADRs — and is "
             "**committed to the GitHub repo** before its local copy is cleaned up.")
lines.append("")
lines.append("## Distribution")
lines.append("")
lines.append("| Tier | ID Range | Count |")
lines.append("|------|----------|-------|")
tier_ranges = {"Advanced": "JAVA-001..100", "Expert": "JAVA-101..250",
               "Architect": "JAVA-251..400", "Enterprise Platform": "JAVA-401..550",
               "Omega": "JAVA-551..700"}
for t in ["Advanced", "Expert", "Architect", "Enterprise Platform", "Omega"]:
    lines.append(f"| {t} | {tier_ranges[t]} | {tier_counts[t]} |")
lines.append("")
lines.append("| Category | Count |")
lines.append("|----------|-------|")
for c in CATEGORIES:
    n = cat_counts[c]
    extra = ""
    if c == "Government / Compliance / Public Infrastructure":
        extra = " (5 primary + 45 cross-industry Omega projects)"
    lines.append(f"| {c} | {n}{extra} |")
lines.append("")
lines.append("> **Industry-distribution guarantee:** Enterprise business ≥100 · FinTech/Banking/Insurance ≥75 · "
             "Healthcare/Pharma ≥75 · Manufacturing/IIoT ≥75 · Telecom ≥50 · Cybersecurity ≥50 · Logistics ≥50 · "
             "Energy ≥50 · Automotive/Aerospace/Transport ≥50 · Data/AI ≥50 · Developer/Platform ≥50 · "
             "Government/Public ≥50 — total exactly **700**.")
lines.append("")
lines.append("## Master Index (all 700)")
lines.append("")
lines.append("| ID | Project | Industry | Architecture | Primary Challenge | Difficulty |")
lines.append("|----|---------|----------|--------------|-------------------|------------|")
for r in catalog:
    arch = r["architecture"].split(";")[0].strip()
    lines.append(f"| {r['id']} | {r['name']} | {r['industry_tag']} | {arch} | "
                 f"{r['core_eng_problem']} | {r['difficulty']} |")
lines.append("")
with open(os.path.join(CATALOG_DIR, "MASTER-INDEX.md"), "w", encoding="utf-8") as f:
    f.write("\n".join(lines))
print("wrote catalog/MASTER-INDEX.md:", len(lines), "lines")

# ---------------- PER-CATEGORY CATALOGS ----------------
by_cat = {}
for r in catalog:
    by_cat.setdefault(r["category"], []).append(r)

for c in CATEGORIES:
    rows = by_cat.get(c, [])
    slug = c.lower().replace("/", "-").replace(" ", "-").replace("--", "-")
    out = []
    out.append(f"# {c} — Catalog")
    out.append("")
    out.append(f"{len(rows)} projects. Full details for every project below; the 13-field design summary "
               "matches the master spec (business problem, engineering problem, architecture, Java stack, "
               "database, messaging, security model, key concepts, industrial rationale).")
    out.append("")
    for r in rows:
        out.append(f"## {r['id']} — {r['name']}")
        out.append("")
        out.append(f"- **Difficulty:** {r['difficulty']} (Tier {r['tier']})")
        out.append(f"- **Industry:** {r['industry_tag']}")
        if r.get("secondary_category"):
            out.append(f"- **Secondary (public-sector) domain:** {r['secondary_category']}")
        out.append(f"- **Business problem:** {r['business_problem']}")
        out.append(f"- **Core engineering problem:** {r['core_eng_problem']}")
        out.append(f"- **Architecture:** {r['architecture']}")
        out.append(f"- **Java technology stack:** {r['primary_tech']}")
        out.append(f"- **Data layer:** {r['database']}")
        out.append(f"- **Messaging:** {r['messaging']}")
        out.append(f"- **Security architecture:** {r['security_model']}")
        out.append(f"- **Key advanced concepts:** {', '.join(r['key_concepts'])}")
        out.append(f"- **Why it is industrial:** {r['why_industrial']}")
        out.append("")
    with open(os.path.join(CATALOG_DIR, f"{slug}.md"), "w", encoding="utf-8") as f:
        f.write("\n".join(out))
    print(f"wrote catalog/{slug}.md: {len(rows)} projects")
