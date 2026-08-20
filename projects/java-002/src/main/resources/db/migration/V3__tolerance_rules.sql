-- Baseline matching tolerances (versioned via row edits by ADMIN role)
INSERT INTO tolerance_rules (id, rule_type, tolerance_pct, action, active) VALUES
('00000000-0000-0000-0000-000000000301','PRICE_VARIANCE',   2.0, 'WARN',  TRUE),
('00000000-0000-0000-0000-000000000302','QUANTITY_VARIANCE',5.0, 'WARN',  TRUE),
('00000000-0000-0000-0000-000000000303','AMOUNT_VARIANCE',  1.0, 'WARN',  TRUE);
