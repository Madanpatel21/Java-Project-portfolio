-- Seed labor-law and fairness rules evaluated by the solver constraints.
INSERT INTO roster_rules (id, code, name, level, threshold, weight, active) VALUES
  ('rule-001', 'SKILL-MATCH',      'Employee must hold the required skill',          'HARD', 1, 100, TRUE),
  ('rule-002', 'AVAILABILITY',     'Employee must be available on the shift day',    'HARD', 1, 100, TRUE),
  ('rule-003', 'ONE-SHIFT-DAY',    'Max one shift per employee per day',             'HARD', 1, 100, TRUE),
  ('rule-004', 'WEEKLY-HOURS',     'Max weekly hours (full-time 40h)',               'HARD', 40, 10, TRUE),
  ('rule-005', 'MIN-REST',         'Min 11h rest after a NIGHT before a MORNING',    'HARD', 11, 50, TRUE),
  ('rule-006', 'MAX-CONSECUTIVE',  'Max 6 working days per rolling week',            'HARD', 6, 40, TRUE),
  ('rule-007', 'NIGHT-CAP',        'Max 4 night shifts per employee per week',       'HARD', 4, 30, TRUE),
  ('rule-008', 'FAIRNESS-HOURS',   'Balance total hours across employees',           'SOFT', 0, 10, TRUE);
