-- Seed policy rules (data-driven policy; engine evaluates comparators generically).
INSERT INTO policy_rules (id, code, category, comparator, threshold, pattern, severity, message, active, sort_order) VALUES
  ('rule-001', 'MEALS-CAP',      'MEALS',        'GREATER_THAN',  75.00, NULL, 'VIOLATION', 'Meal amount exceeds per-meal cap of 75.00',                     TRUE, 10),
  ('rule-002', 'MEALS-RECEIPT',  'MEALS',        'MISSING_RECEIPT', NULL, NULL, 'WARNING',  'Meal above 30.00 requires a receipt reference',                 TRUE, 20),
  ('rule-003', 'LODGING-CAP',    'LODGING',      'GREATER_THAN', 400.00, NULL, 'VIOLATION', 'Lodging amount exceeds per-night cap of 400.00',                TRUE, 30),
  ('rule-004', 'LODGING-RECEIPT','LODGING',      'MISSING_RECEIPT', NULL, NULL, 'WARNING',  'Lodging claim requires a receipt reference',                    TRUE, 40),
  ('rule-005', 'MILEAGE-CAP',    'MILEAGE',      'GREATER_THAN', 300.00, NULL, 'VIOLATION', 'Mileage amount exceeds per-day cap of 300.00',                  TRUE, 50),
  ('rule-006', 'ENT-CAP',        'ENTERTAINMENT', 'GREATER_THAN', 200.00, NULL, 'VIOLATION', 'Entertainment amount exceeds cap of 200.00',                    TRUE, 60),
  ('rule-007', 'SUPPLIES-CAP',   'SUPPLIES',     'GREATER_THAN', 500.00, NULL, 'VIOLATION', 'Office supplies amount exceeds cap of 500.00',                  TRUE, 70),
  ('rule-008', 'ROUND-AMOUNT',   'ANY',          'ROUND_AMOUNT',    NULL, NULL, 'WARNING',  'Suspiciously round amount (common in fabricated receipts)',     TRUE, 80),
  ('rule-009', 'ATM-BLOCKER',    'ANY',          'MERCHANT_CONTAINS', NULL, 'ATM', 'BLOCKER',  'Cash withdrawal via ATM is not a reimbursable expense',         TRUE, 90);
