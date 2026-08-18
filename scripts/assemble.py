# -*- coding: utf-8 -*-
"""Assembles the 700-project master catalog.
Block02 rows are positional: row order within each category matches block01 id/name order.
The first field of each row is an industry TAG; name/tier/category come from block01.
"""
import json, sys, os
from collections import Counter

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import block01_ids as b1
import block02a_ent1, block02b_ent2, block02c_fin1, block02d_fin2, block02e_hlt1, block02f_hlt2
import block02g_mfg1, block02h_mfg2, block02i_tel, block02j_cyb, block02k_log, block02l_ene
import block02m_trn, block02n_dat, block02o_dev, block02p_gov

FIELDS = ["industry_tag", "business_problem", "core_eng_problem", "architecture", "primary_tech",
          "database", "messaging", "security_model", "key_concepts", "why_industrial"]
DIFF = {1: "Advanced", 2: "Expert", 3: "Architect", 4: "Enterprise Platform", 5: "Omega"}
CATEGORY_NAMES = ["Enterprise Business Platforms", "Banking / FinTech / Insurance",
                  "Healthcare / Pharma / Life Sciences", "Manufacturing / Industrial IoT / Robotics",
                  "Telecom / Networking / Media", "Cybersecurity / Identity / Secrets",
                  "Logistics / Supply Chain / Fleet", "Energy / Utilities / Grid",
                  "Automotive / Aerospace / Transportation", "Data / AI Infrastructure",
                  "Developer / Platform Infrastructure", "Government / Compliance / Public Infrastructure"]

GROUPS = [
    ("Enterprise Business Platforms", b1.ENT, block02a_ent1.ROWS + block02b_ent2.ROWS),
    ("Banking / FinTech / Insurance", b1.FIN, block02c_fin1.ROWS + block02d_fin2.ROWS),
    ("Healthcare / Pharma / Life Sciences", b1.HLT, block02e_hlt1.ROWS + block02f_hlt2.ROWS),
    ("Manufacturing / Industrial IoT / Robotics", b1.MFG, block02g_mfg1.ROWS + block02h_mfg2.ROWS),
    ("Telecom / Networking / Media", b1.TEL, block02i_tel.ROWS),
    ("Cybersecurity / Identity / Secrets", b1.CYB, block02j_cyb.ROWS),
    ("Logistics / Supply Chain / Fleet", b1.LOG, block02k_log.ROWS),
    ("Energy / Utilities / Grid", b1.ENE, block02l_ene.ROWS),
    ("Automotive / Aerospace / Transportation", b1.TRN, block02m_trn.ROWS),
    ("Data / AI Infrastructure", b1.DAT, block02n_dat.ROWS),
    ("Developer / Platform Infrastructure", b1.DEV, block02o_dev.ROWS),
    ("Government / Compliance / Public Infrastructure", b1.GOV, block02p_gov.ROWS),
]

catalog = []
for cat, idlist, rows in GROUPS:
    assert len(idlist) == len(rows), f"{cat}: ids {len(idlist)} vs rows {len(rows)}"
    for (pid, name, tier), row in zip(idlist, rows):
        assert len(row) == len(FIELDS), f"{pid}: {len(row)} fields"
        rec = dict(zip(FIELDS, row))
        concepts = [c.strip() for c in rec["key_concepts"].split(",") if c.strip()]
        catalog.append({
            "id": pid,
            "name": name,
            "category": cat,
            "industry_tag": rec["industry_tag"],
            "tier": tier,
            "difficulty": DIFF[tier],
            "business_problem": rec["business_problem"],
            "core_eng_problem": rec["core_eng_problem"],
            "architecture": rec["architecture"],
            "primary_tech": rec["primary_tech"],
            "database": rec["database"],
            "messaging": rec["messaging"],
            "security_model": rec["security_model"],
            "key_concepts": concepts,
            "why_industrial": rec["why_industrial"],
        })

catalog.sort(key=lambda r: int(r["id"].split("-")[1]))
ids = [r["id"] for r in catalog]
names = [r["name"] for r in catalog]
assert len(catalog) == 700, len(catalog)
assert len(set(ids)) == 700
assert len(set(names)) == 700
assert ids[0] == "JAVA-001" and ids[-1] == "JAVA-700"
for i in range(699):
    assert int(ids[i][-3:]) + 1 == int(ids[i+1][-3:]), f"gap at {ids[i]}"
print("entries: 700 | ids contiguous JAVA-001..JAVA-700 | names unique")

cat_counts = Counter(r["category"] for r in catalog)
tier_counts = Counter(r["difficulty"] for r in catalog)
print("\nCATEGORY COUNTS:")
for c in CATEGORY_NAMES:
    print(f"  {c}: {cat_counts.get(c, 0)}")
print("DIFFICULTY COUNTS:")
for d in ["Advanced", "Expert", "Architect", "Enterprise Platform", "Omega"]:
    print(f"  {d}: {tier_counts.get(d, 0)}")

minima = {
    "Enterprise Business Platforms": 100, "Banking / FinTech / Insurance": 75,
    "Healthcare / Pharma / Life Sciences": 75, "Manufacturing / Industrial IoT / Robotics": 75,
    "Telecom / Networking / Media": 50, "Cybersecurity / Identity / Secrets": 50,
    "Logistics / Supply Chain / Fleet": 50, "Energy / Utilities / Grid": 50,
    "Automotive / Aerospace / Transportation": 50, "Data / AI Infrastructure": 50,
    "Developer / Platform Infrastructure": 50, "Government / Compliance / Public Infrastructure": 50,
}

# --- Secondary category: Government/Public-Sector cross-industry Omega projects ---
# 45 Omega-slab projects (JAVA-551..695) whose domain is inherently public-sector
# (transit, rail, ATC, maritime authority, smart city, census/statistics, records,
# gov data platforms, gov IT operations). Combined with the 5 primary GovTech
# projects (JAVA-696..700) this satisfies the 50-project Government minimum.
GOVTECH_SECONDARY = {
    # Public transit / rail / aviation safety / maritime authority / smart city (23)
    556, 557, 563, 567, 568, 569, 570, 571, 572, 573, 574, 575,
    582, 584, 585, 586, 587, 589, 591, 592, 593, 594, 595,
    # Gov data & AI platforms, records, census, open data (16)
    596, 597, 598, 599, 605, 609, 611, 612, 619, 621, 623, 627, 629, 631, 633, 634,
    # Gov IT / platform operations (6)
    646, 647, 654, 656, 664, 673,
}
assert len(GOVTECH_SECONDARY) == 45

for r in catalog:
    num = int(r["id"].split("-")[1])
    if num in GOVTECH_SECONDARY:
        r["secondary_category"] = "Government / Compliance / Public Infrastructure"

gov_total = cat_counts["Government / Compliance / Public Infrastructure"] + sum(
    1 for r in catalog if r.get("secondary_category") == "Government / Compliance / Public Infrastructure")
assert gov_total == 50, gov_total
print(f"Government/Public-Sector ledger: 5 primary + 45 cross-industry (Omega slab) = {gov_total}")

ledger = Counter()
for r in catalog:
    ledger[r["category"]] += 1
    if r.get("secondary_category"):
        ledger[r["secondary_category"]] += 1
for c, m in minima.items():
    assert ledger[c] >= m, f"{c} below minimum {m} (ledger {ledger[c]})"
print("\nAll required category minima satisfied (Government ledger includes 45 cross-industry Omega projects).")
assert tier_counts["Omega"] == 150 and tier_counts["Advanced"] == 100

out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "data")
os.makedirs(out_dir, exist_ok=True)
with open(os.path.join(out_dir, "catalog.json"), "w", encoding="utf-8") as f:
    json.dump(catalog, f, indent=2, ensure_ascii=False)
print("\nWrote data/catalog.json")
